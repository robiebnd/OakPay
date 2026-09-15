"use client";

import {
  Bell,
  LockKeyhole,
  Settings as SettingsIcon,
  ShieldCheck,
} from "lucide-react";

import AdminShell from "../../components/AdminShell";

export default function SettingsPage() {
  return (
    <AdminShell>
      <div className="space-y-8">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">
            Administration
          </p>

          <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-[#111827]">
            Settings
          </h1>

          <p className="mt-2 max-w-2xl text-sm leading-6 text-[#6b7280]">
            Configure OakPay administration preferences and
            operational controls.
          </p>
        </div>

        <div className="grid gap-5 lg:grid-cols-2">
          <SettingCard
            icon={<ShieldCheck size={21} />}
            title="Security"
            description="Manage administrative security and access controls."
          />

          <SettingCard
            icon={<Bell size={21} />}
            title="Notifications"
            description="Configure operational alerts and administrator notifications."
          />

          <SettingCard
            icon={<LockKeyhole size={21} />}
            title="Access Control"
            description="Manage administrator roles and account access."
          />

          <SettingCard
            icon={<SettingsIcon size={21} />}
            title="System Preferences"
            description="Configure OakPay administration preferences."
          />
        </div>

        <section className="rounded-2xl border border-[#dce8df] bg-[#f4f9f4] p-6">
          <div className="flex gap-4">
            <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-white text-[#397b0a] shadow-sm">
              <SettingsIcon size={20} />
            </div>

            <div>
              <h2 className="text-base font-extrabold text-[#082d16]">
                Administration settings
              </h2>

              <p className="mt-1 text-sm leading-6 text-[#5f6b63]">
                Settings controls are being introduced progressively
                as the corresponding OakPay administration APIs are
                implemented.
              </p>
            </div>
          </div>
        </section>
      </div>
    </AdminShell>
  );
}

function SettingCard({
  icon,
  title,
  description,
}: {
  icon: React.ReactNode;
  title: string;
  description: string;
}) {
  return (
    <section className="rounded-2xl border border-[#e3e8e5] bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
      <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#edf5ee] text-[#397b0a]">
        {icon}
      </div>

      <h2 className="mt-5 text-base font-extrabold text-[#111827]">
        {title}
      </h2>

      <p className="mt-2 text-sm leading-6 text-[#6b7280]">
        {description}
      </p>

      <span className="mt-5 inline-flex rounded-full bg-[#f3f5f4] px-3 py-1 text-xs font-bold text-[#6b7280]">
        Configuration
      </span>
    </section>
  );
}