import type { LucideIcon } from "lucide-react";

type StatCardProps = {
  label: string;
  value: string | number;
  description?: string;
  icon: LucideIcon;
  tone?: "green" | "blue" | "amber" | "red";
};

const tones = {
  green: {
    icon: "bg-[#eaf3e5] text-[#397b0a]",
    value: "text-[#111827]",
  },
  blue: {
    icon: "bg-blue-50 text-blue-700",
    value: "text-[#111827]",
  },
  amber: {
    icon: "bg-amber-50 text-amber-700",
    value: "text-[#111827]",
  },
  red: {
    icon: "bg-red-50 text-red-700",
    value: "text-[#111827]",
  },
};

export default function StatCard({
  label,
  value,
  description,
  icon: Icon,
  tone = "green",
}: StatCardProps) {
  const style = tones[tone];

  return (
    <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-xs font-bold uppercase tracking-wide text-[#6b7280]">
            {label}
          </p>

          <p
            className={`mt-2 text-3xl font-extrabold tracking-tight ${style.value}`}
          >
            {value}
          </p>

          {description ? (
            <p className="mt-1 text-xs text-[#6b7280]">
              {description}
            </p>
          ) : null}
        </div>

        <div
          className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${style.icon}`}
        >
          <Icon size={21} strokeWidth={2} />
        </div>
      </div>
    </div>
  );
}