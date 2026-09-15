"use client";

import {
  Search,
  Users,
  UserCheck,
  UserX,
  ShieldCheck,
} from "lucide-react";

import AdminShell from "../../components/AdminShell";

export default function ClientsPage() {
  return (
    <AdminShell>
      <div className="space-y-8">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">
            Administration
          </p>

          <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-[#111827]">
            Clients
          </h1>

          <p className="mt-2 max-w-2xl text-sm leading-6 text-[#6b7280]">
            Manage OakPay client accounts, account status and
            verification information.
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <StatCard
            label="Total Clients"
            value="—"
            icon={<Users size={20} />}
          />

          <StatCard
            label="Active"
            value="—"
            icon={<UserCheck size={20} />}
          />

          <StatCard
            label="Inactive"
            value="—"
            icon={<UserX size={20} />}
          />

          <StatCard
            label="KYC Verified"
            value="—"
            icon={<ShieldCheck size={20} />}
          />
        </div>

        <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          <div className="border-b border-[#e3e8e5] p-5">
            <div className="relative max-w-xl">
              <Search
                size={18}
                className="absolute left-4 top-1/2 -translate-y-1/2 text-[#9ca3af]"
              />

              <input
                type="text"
                placeholder="Search clients by name, email or client ID..."
                className="w-full rounded-xl border border-[#dce3df] bg-[#fafcfb] py-3 pl-11 pr-4 text-sm outline-none focus:border-[#397b0a] focus:ring-2 focus:ring-[#397b0a]/10"
              />
            </div>
          </div>

          <div className="flex min-h-[300px] flex-col items-center justify-center px-6 text-center">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#edf5ee] text-[#397b0a]">
              <Users size={26} />
            </div>

            <h2 className="mt-4 text-base font-extrabold text-[#111827]">
              Client Management
            </h2>

            <p className="mt-2 max-w-md text-sm leading-6 text-[#6b7280]">
              The client management workspace is ready. Live
              client records can be connected to the administration
              API here.
            </p>
          </div>
        </section>
      </div>
    </AdminShell>
  );
}

function StatCard({
  label,
  value,
  icon,
}: {
  label: string;
  value: string;
  icon: React.ReactNode;
}) {
  return (
    <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-bold uppercase tracking-wider text-[#6b7280]">
            {label}
          </p>

          <p className="mt-3 text-3xl font-extrabold text-[#111827]">
            {value}
          </p>
        </div>

        <div className="rounded-xl bg-[#edf5ee] p-3 text-[#397b0a]">
          {icon}
        </div>
      </div>
    </div>
  );
}