/**
 * fetchUtils — two helpers that fix the two biggest load-time problems:
 *
 *  1. parallelLimit   — runs N async tasks concurrently but caps at `limit`
 *                       (browsers allow ~6 HTTP/1.1 connections per host;
 *                        blasting 200 at once fills the queue and slows everything)
 *
 *  2. cachedFetch     — simple in-memory TTL cache so every page navigation
 *                       doesn't re-fetch the full clients + securities list
 */

// ─── 1. Concurrency-capped parallel execution ─────────────────────────────────

/**
 * Like Promise.allSettled but runs at most `limit` tasks at a time.
 * Default concurrency = 6 (matches the browser's per-host connection pool).
 */
export async function parallelLimit<T>(
  tasks: Array<() => Promise<T>>,
  limit = 6,
): Promise<PromiseSettledResult<T>[]> {
  if (tasks.length === 0) return [];

  const results: PromiseSettledResult<T>[] = new Array(tasks.length);
  let next = 0;

  async function worker() {
    while (next < tasks.length) {
      const i = next++;
      try {
        results[i] = { status: 'fulfilled', value: await tasks[i]() };
      } catch (reason) {
        results[i] = { status: 'rejected', reason };
      }
    }
  }

  // Launch min(limit, tasks.length) workers — each grabs the next task when free
  await Promise.all(
    Array.from({ length: Math.min(limit, tasks.length) }, worker),
  );
  return results;
}

// ─── 2. In-memory TTL cache ────────────────────────────────────────────────────

interface CacheEntry {
  data: unknown;
  expiresAt: number;
}

const _store = new Map<string, CacheEntry>();

/**
 * Call `fn` and cache the result under `key` for `ttlMs` milliseconds.
 * On the next call within the TTL the cached value is returned immediately.
 *
 * Usage:
 *   const clients = await cachedFetch('clients', getAllClients, 60_000);
 */
export async function cachedFetch<T>(
  key: string,
  fn: () => Promise<T>,
  ttlMs = 60_000,
): Promise<T> {
  const hit = _store.get(key);
  if (hit && Date.now() < hit.expiresAt) return hit.data as T;

  const data = await fn();
  _store.set(key, { data, expiresAt: Date.now() + ttlMs });
  return data;
}

/** Drop a specific key (e.g. after a mutation) or the entire cache. */
export function invalidateCache(key?: string) {
  if (key) _store.delete(key);
  else _store.clear();
}
