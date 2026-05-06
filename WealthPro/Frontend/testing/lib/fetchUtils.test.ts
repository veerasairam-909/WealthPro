import { describe, it, expect, vi, beforeEach } from 'vitest';
import { parallelLimit, cachedFetch, invalidateCache } from '@/lib/fetchUtils';

// Clear cache before every test so tests don't interfere with each other
beforeEach(() => {
  invalidateCache();
});

// ─── parallelLimit ────────────────────────────────────────────────────────────

describe('parallelLimit', () => {
  it('returns an empty array when given no tasks', async () => {
    const results = await parallelLimit([]);
    expect(results).toEqual([]);
  });

  it('resolves all fulfilled tasks and returns fulfilled results', async () => {
    const tasks = [
      () => Promise.resolve(10),
      () => Promise.resolve(20),
      () => Promise.resolve(30),
    ];
    const results = await parallelLimit(tasks);
    expect(results).toHaveLength(3);
    expect(results[0].status).toBe('fulfilled');
    expect((results[0] as PromiseFulfilledResult<number>).value).toBe(10);
    expect((results[2] as PromiseFulfilledResult<number>).value).toBe(30);
  });

  it('captures rejected tasks without throwing and marks them as rejected', async () => {
    const tasks = [
      () => Promise.reject(new Error('oops')),
      () => Promise.resolve('ok'),
    ];
    const results = await parallelLimit(tasks);
    expect(results[0].status).toBe('rejected');
    expect(results[1].status).toBe('fulfilled');
  });

  it('handles a mix of fulfilled and rejected tasks', async () => {
    const tasks = [
      () => Promise.resolve(1),
      () => Promise.reject(new Error('fail')),
      () => Promise.resolve(3),
    ];
    const results = await parallelLimit(tasks);
    expect(results[0].status).toBe('fulfilled');
    expect(results[1].status).toBe('rejected');
    expect(results[2].status).toBe('fulfilled');
  });

  it('respects the concurrency limit', async () => {
    let running = 0;
    let maxConcurrent = 0;

    const tasks = Array.from({ length: 9 }, () => () =>
      new Promise<number>((resolve) => {
        running++;
        maxConcurrent = Math.max(maxConcurrent, running);
        setTimeout(() => {
          running--;
          resolve(1);
        }, 10);
      }),
    );

    await parallelLimit(tasks, 3);
    expect(maxConcurrent).toBeLessThanOrEqual(3);
  });

  it('processes a single task correctly', async () => {
    const results = await parallelLimit([() => Promise.resolve('single')]);
    expect(results).toHaveLength(1);
    expect((results[0] as PromiseFulfilledResult<string>).value).toBe('single');
  });
});

// ─── cachedFetch ─────────────────────────────────────────────────────────────

describe('cachedFetch', () => {
  it('calls the fetch function and returns its result', async () => {
    const fn = vi.fn().mockResolvedValue('data');
    const result = await cachedFetch('key1', fn);
    expect(result).toBe('data');
    expect(fn).toHaveBeenCalledTimes(1);
  });

  it('returns the cached result on the second call without calling fn again', async () => {
    const fn = vi.fn().mockResolvedValue('cached-value');
    await cachedFetch('key2', fn);
    const result = await cachedFetch('key2', fn);
    expect(result).toBe('cached-value');
    expect(fn).toHaveBeenCalledTimes(1);
  });

  it('caches different keys independently', async () => {
    const fn1 = vi.fn().mockResolvedValue('value-a');
    const fn2 = vi.fn().mockResolvedValue('value-b');
    const r1 = await cachedFetch('alpha', fn1);
    const r2 = await cachedFetch('beta', fn2);
    expect(r1).toBe('value-a');
    expect(r2).toBe('value-b');
    expect(fn1).toHaveBeenCalledTimes(1);
    expect(fn2).toHaveBeenCalledTimes(1);
  });

  it('re-fetches after the TTL expires', async () => {
    vi.useFakeTimers();
    const fn = vi.fn().mockResolvedValue('fresh');

    await cachedFetch('ttl-key', fn, 100);
    vi.advanceTimersByTime(200);
    await cachedFetch('ttl-key', fn, 100);

    expect(fn).toHaveBeenCalledTimes(2);
    vi.useRealTimers();
  });

  it('does not re-fetch before the TTL expires', async () => {
    vi.useFakeTimers();
    const fn = vi.fn().mockResolvedValue('data');

    await cachedFetch('short-ttl', fn, 500);
    vi.advanceTimersByTime(100);
    await cachedFetch('short-ttl', fn, 500);

    expect(fn).toHaveBeenCalledTimes(1);
    vi.useRealTimers();
  });
});

// ─── invalidateCache ─────────────────────────────────────────────────────────

describe('invalidateCache', () => {
  it('forces a re-fetch for the invalidated key', async () => {
    const fn = vi.fn().mockResolvedValue('value');
    await cachedFetch('mykey', fn);
    invalidateCache('mykey');
    await cachedFetch('mykey', fn);
    expect(fn).toHaveBeenCalledTimes(2);
  });

  it('does not affect other keys when a specific key is invalidated', async () => {
    const fn = vi.fn().mockResolvedValue('x');
    await cachedFetch('keep', fn);
    await cachedFetch('remove', fn);
    invalidateCache('remove');
    await cachedFetch('keep', fn); // should still be cached
    expect(fn).toHaveBeenCalledTimes(2); // only the 'remove' key re-fetches
  });

  it('clears all cached entries when called with no argument', async () => {
    const fn1 = vi.fn().mockResolvedValue('a');
    const fn2 = vi.fn().mockResolvedValue('b');
    await cachedFetch('k1', fn1);
    await cachedFetch('k2', fn2);
    invalidateCache();
    await cachedFetch('k1', fn1);
    await cachedFetch('k2', fn2);
    expect(fn1).toHaveBeenCalledTimes(2);
    expect(fn2).toHaveBeenCalledTimes(2);
  });
});
