type StatusPillProps = {
  label: string;
  value: string;
};

export function StatusPill({ label, value }: StatusPillProps) {
  return (
    <div className="inline-flex items-center gap-2 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-950">
      <span className="h-2 w-2 rounded-full bg-emerald-500" />
      <span className="font-medium">{label}</span>
      <span className="text-emerald-700">{value}</span>
    </div>
  );
}
