import { createStore } from 'jotai';

const theStore = createStore();

export type MyStore = typeof theStore;

export function getStore(curStore?: MyStore): MyStore {
  return curStore || theStore;
}
