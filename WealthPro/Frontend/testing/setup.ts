import '@testing-library/jest-dom';

// Vitest 2.x passes --localstorage-file to jsdom workers for localStorage isolation.
// When the temp-file path is invalid, jsdom may replace window.localStorage with a
// broken stub (missing getItem/setItem). We polyfill it here so modules that access
// localStorage at import time (e.g. auth/store.ts) always see a proper object.
const _localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem:    (key: string)              => store[key] ?? null,
    setItem:    (key: string, val: string) => { store[key] = String(val); },
    removeItem: (key: string)              => { delete store[key]; },
    clear:      ()                         => { store = {}; },
    get length()                           { return Object.keys(store).length; },
    key:        (i: number)                => Object.keys(store)[i] ?? null,
  };
})();

vi.stubGlobal('localStorage', _localStorageMock);
