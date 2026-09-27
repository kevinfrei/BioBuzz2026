import { ReactElement } from 'react';
import { useAtom } from 'jotai';

import { Plus, Search, Trash2 } from 'lucide-react';

import { chkRef } from './dto_schema';
import {
  namedValuesAtom,
  searchFilterAtom,
  selectedKeyAtom,
  toastAtom,
} from './state';
import { ValRefInline } from './ValRefs';

// Values Editor
export function ValuesEditor(): ReactElement {
  const [namedValues, setNamedValues] = useAtom(namedValuesAtom);
  const [selected, setSelected] = useAtom(selectedKeyAtom);
  const [search, setSearch] = useAtom(searchFilterAtom);
  const [, setToast] = useAtom(toastAtom);

  const values = namedValues.values || {};
  const keys = Object.keys(values).filter((k) =>
    k.toLowerCase().includes(search.toLowerCase()),
  );

  const activeKey = selected.store === 'values' ? selected.key : keys[0] || '';
  const activeValue = values[activeKey];

  const handleAdd = () => {
    let baseName = 'newValue';
    let count = 1;
    while (values[`${baseName}${count}`]) count++;
    const newKey = `${baseName}${count}`;

    setNamedValues({
      ...namedValues,
      values: { ...values, [newKey]: { val: 0.0 } },
    });
    setSelected({ store: 'values', key: newKey });
    setToast(`Added value "${newKey}"`);
  };

  const handleRename = (oldKey: string, newKey: string) => {
    if (!newKey || oldKey === newKey || values[newKey]) return;
    const newDict = { ...values };
    newDict[newKey] = newDict[oldKey];
    delete newDict[oldKey];

    setNamedValues({ ...namedValues, values: newDict });
    setSelected({ store: 'values', key: newKey });
  };

  const handleDelete = (keyToDelete: string) => {
    const newDict = { ...values };
    delete newDict[keyToDelete];
    setNamedValues({ ...namedValues, values: newDict });

    const remaining = Object.keys(newDict);
    setSelected({ store: 'values', key: remaining[0] || '' });
    setToast(`Deleted value "${keyToDelete}"`);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 h-full">
      {/* Left List */}
      <div className="md:col-span-1 border border-neutral-200 dark:border-neutral-800 rounded-xl p-4 bg-white dark:bg-neutral-900 flex flex-col space-y-3">
        <div className="flex items-center justify-between">
          <span className="font-bold text-sm text-neutral-800 dark:text-neutral-200">
            Values Store ({Object.keys(values).length})
          </span>
          <button
            type="button"
            onClick={handleAdd}
            className="p-1.5 rounded-md bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1 text-xs font-semibold">
            <Plus className="w-3.5 h-3.5" /> Add
          </button>
        </div>

        <div className="relative">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-neutral-400" />
          <input
            type="text"
            placeholder="Search values..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-8 pr-3 py-1.5 text-xs rounded-md border border-neutral-300 dark:border-neutral-700 bg-neutral-50 dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 outline-none focus:ring-2 focus:ring-sky-500"
          />
        </div>

        <div className="flex-grow overflow-y-auto space-y-1 pr-1">
          {keys.length === 0 ? (
            <div className="text-center text-xs text-neutral-400 py-6">
              No values found.
            </div>
          ) : (
            keys.map((k) => (
              <div
                key={k}
                onClick={() => setSelected({ store: 'values', key: k })}
                className={`p-2.5 rounded-lg text-xs cursor-pointer flex items-center justify-between transition-all ${
                  activeKey === k
                    ? 'bg-sky-100 dark:bg-sky-950/80 border border-sky-300 dark:border-sky-800 text-sky-900 dark:text-sky-200 font-semibold'
                    : 'hover:bg-neutral-100 dark:hover:bg-neutral-800 text-neutral-700 dark:text-neutral-300'
                }`}>
                <span className="truncate">{k}</span>
                <span className="font-mono text-neutral-500 dark:text-neutral-400">
                  <ValRefInline valref={values[k]} />
                </span>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Right Detail Editor */}
      <div className="md:col-span-2 border border-neutral-200 dark:border-neutral-800 rounded-xl p-5 bg-white dark:bg-neutral-900 flex flex-col space-y-4">
        {activeKey && activeValue ? (
          <>
            <div className="flex items-center justify-between border-b border-neutral-200 dark:border-neutral-800 pb-3">
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  defaultValue={activeKey}
                  key={activeKey}
                  onBlur={(e) => handleRename(activeKey, e.target.value.trim())}
                  className="font-bold text-lg text-neutral-900 dark:text-neutral-100 bg-transparent border-b border-dashed border-neutral-400 focus:border-sky-500 outline-none px-1"
                />
                <span className="text-xs text-neutral-400 font-mono">
                  (Value Key)
                </span>
              </div>
              <button
                type="button"
                onClick={() => handleDelete(activeKey)}
                className="p-1.5 rounded text-neutral-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-all"
                title="Delete Key">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-4 max-w-md">
              <div>
                <label className="text-xs font-semibold text-neutral-700 dark:text-neutral-300 uppercase tracking-wider block mb-1">
                  Numerical Value
                </label>
                <input
                  type="number"
                  step="any"
                  value={
                    chkRef(activeValue) ? activeValue.ref : activeValue.val
                  }
                  onChange={(e) => {
                    const newVal = parseFloat(e.target.value) || 0;
                    setNamedValues({
                      ...namedValues,
                      values: { ...values, [activeKey]: { val: newVal } },
                    });
                  }}
                  className="w-full px-3 py-2 rounded-lg border border-neutral-300 dark:border-neutral-700 bg-neutral-50 dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none text-sm font-mono"
                />
              </div>
            </div>
          </>
        ) : (
          <div className="flex-grow flex items-center justify-center text-neutral-400 text-xs">
            Select or create a value to edit.
          </div>
        )}
      </div>
    </div>
  );
}
