import { useEffect } from 'react';
import { useAtom } from 'jotai';

import { Sparkles } from 'lucide-react';

import { toastAtom } from './state';

export function NotificationToast() {
  const [toast, setToast] = useAtom(toastAtom);
  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast, setToast]);

  if (!toast) return null;

  return (
    <div className="fixed bottom-5 right-5 z-50 flex items-center gap-3 px-4 py-3 rounded-md bg-neutral-900 text-white shadow-xl border border-neutral-700 animate-bounce-short">
      <Sparkles className="w-4 h-4 text-sky-400" />
      <span className="text-sm font-medium">{toast}</span>
    </div>
  );
}
