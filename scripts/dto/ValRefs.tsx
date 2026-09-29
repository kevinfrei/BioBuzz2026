import { ReactElement } from 'react';
import { useAtomValue } from 'jotai';

import { AlertFilled } from '@fluentui/react-icons';
import { isUndefined } from '@freik/typechk';

import { chkErr, chkRef, ResolvedValue, ValRef } from './dto_schema';
import { resolveValRef } from './Resolvers';
import { symbolTableAtom } from './state';

export type ValRefControlProps = {
  label: string;
  value: ValRef;
  onChange: (valRef: ValRef) => void;
};

// ValRef Control: Switch between Inline ({ val }) and Ref ({ ref })
export function ValRefControl({
  label,
  value,
  onChange,
}: ValRefControlProps): ReactElement {
  const symbolTable = useAtomValue(symbolTableAtom);
  const valueKeys = [...symbolTable.values.keys()];
  const isRef = chkRef(value);
  const resolved = resolveValRef(value, symbolTable);

  const setToRef = (toRef: boolean) => {
    if (toRef) {
      const firstKey = valueKeys[0] || '';
      onChange({ ref: firstKey });
    } else {
      onChange({ val: chkErr(resolved) ? 0 : resolved });
    }
  };

  return (
    <div className="p-3 rounded-lg border border-neutral-200 dark:border-neutral-800 bg-neutral-50 dark:bg-neutral-900/60 transition-all">
      <div className="flex items-center justify-between mb-2">
        <label className="text-xs font-semibold text-neutral-700 dark:text-neutral-300 uppercase tracking-wider">
          {label}
        </label>
        <div className="flex items-center bg-neutral-200 dark:bg-neutral-800 p-0.5 rounded-md text-xs">
          <button
            type="button"
            onClick={() => setToRef(false)}
            className={`px-2 py-0.5 rounded ${
              !isRef
                ? 'bg-white dark:bg-neutral-700 text-sky-600 dark:text-sky-400 font-medium shadow-sm'
                : 'text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200'
            }`}>
            Inline Value
          </button>
          <button
            type="button"
            onClick={() => setToRef(true)}
            className={`px-2 py-0.5 rounded ${
              isRef
                ? 'bg-white dark:bg-neutral-700 text-sky-600 dark:text-sky-400 font-medium shadow-sm'
                : 'text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200'
            }`}>
            Reference
          </button>
        </div>
      </div>

      {!isRef ? (
        <div className="flex items-center gap-2">
          <input
            type="number"
            step="any"
            value={value && 'val' in value ? value.val : 0}
            onChange={(e) => onChange({ val: parseFloat(e.target.value) || 0 })}
            className="w-full px-3 py-1.5 text-sm rounded-md border border-neutral-300 dark:border-neutral-700 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none"
          />
        </div>
      ) : (
        <div className="space-y-2">
          <select
            value={value?.ref || ''}
            onChange={(e) => onChange({ ref: e.target.value })}
            className="w-full px-3 py-1.5 text-sm rounded-md border border-neutral-300 dark:border-neutral-700 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none">
            <option value="" disabled>
              Select Value Reference...
            </option>
            {valueKeys.map((k) => (
              <option key={k} value={k}>
                {k} <ValRefInline valref={symbolTable.values.get(k)} />
              </option>
            ))}
          </select>
          {chkErr(resolved) && (
            <div className="flex items-center gap-1.5 text-xs text-rose-500 dark:text-rose-400">
              <AlertFilled />
              <span>Missing reference: "{value?.ref}"</span>
            </div>
          )}
        </div>
      )}

      {/* Resolved summary badge */}
      <div className="mt-2 text-right">
        <span
          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-mono ${
            !chkErr(resolved)
              ? 'bg-sky-100 dark:bg-sky-950/60 text-sky-700 dark:text-sky-300 border border-sky-200 dark:border-sky-800/50'
              : 'bg-rose-100 dark:bg-rose-950/60 text-rose-700 dark:text-rose-300 border border-rose-200 dark:border-rose-800/50'
          }`}>
          <span>Resolved:</span>
          <strong className="font-bold">
            <ResolvedValueInline value={resolved} />
          </strong>
        </span>
      </div>
    </div>
  );
}

export function ResolvedValueInline({
  value,
}: {
  value: ResolvedValue;
}): ReactElement {
  return <>{chkErr(value) ? `Error: ${value.err}` : value.toFixed(2)}</>;
}

export function ValRefInline({
  valref,
}: {
  valref?: ValRef | undefined;
}): ReactElement {
  if (isUndefined(valref)) {
    return <>Not found</>;
  }
  const isRef = chkRef(valref);
  const title = isRef ? 'Ref' : 'Value';
  const data = isRef ? valref.ref : valref.val.toFixed(1);
  return (
    <>
      {title} <code>{data}</code>
    </>
  );
}
