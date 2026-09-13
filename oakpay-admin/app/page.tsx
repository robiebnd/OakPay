"use client";

import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  FileCheck2,
  MessageSquare,
  ShieldAlert,
} from "lucide-react";

import AdminShell from "../components/AdminShell";
import StatCard from "../components/StatCard";
import { useAdminDashboard } from "../hooks/useAdminDashboard";

export default function Dashboard() {
  const {
    data,
    loading,
    error,
    reload,
  } = useAdminDashboard();

  return (
    <AdminShell>
      <div className="space-y-8">
        {/* Heading */}
        <div className="flex flex-col justify-between gap-4 md:flex-row md:items-end">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">
              Operations overview
            </p>

            <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-[#111827]">
              Dashboard
            </h1>

            <p className="mt-2 max-w-2xl text-sm text-[#6b7280]">
              Monitor OakPay client operations, compliance activity,
              queries and P2P resolutions from one workspace.
            </p>
          </div>

          <button
            type="button"
            onClick={reload}
            disabled={loading}
            className="rounded-xl border border-[#dce3df] bg-white px-4 py-2.5 text-sm font-bold text-[#374151] shadow-sm transition hover:bg-[#f8faf9] disabled:opacity-50"
          >
            {loading ? "Refreshing..." : "Refresh data"}
          </button>
        </div>

        {/* Error */}
        {error ? (
          <div className="flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50 p-5">
            <ShieldAlert className="mt-0.5 text-red-600" size={20} />

            <div>
              <p className="text-sm font-bold text-red-800">
                Dashboard data unavailable
              </p>

              <p className="mt-1 text-xs text-red-700">
                {error}
              </p>
            </div>
          </div>
        ) : null}

        {/* KPI strip */}
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <StatCard
            label="Pending KYC"
            value={loading ? "—" : data?.pendingKyc ?? 0}
            description="Awaiting compliance review"
            icon={FileCheck2}
            tone="amber"
          />

          <StatCard
            label="Open Queries"
            value={loading ? "—" : data?.openQueries ?? 0}
            description="Client requests requiring action"
            icon={MessageSquare}
            tone="blue"
          />

          <StatCard
            label="Active Disputes"
            value={loading ? "—" : data?.activeDisputes ?? 0}
            description="P2P cases currently active"
            icon={AlertTriangle}
            tone="red"
          />

          <StatCard
            label="Pending Resolutions"
            value={loading ? "—" : data?.pendingResolutions ?? 0}
            description="Cases awaiting resolution"
            icon={Clock3}
            tone="green"
          />
        </div>

        {/* Operations */}
        <div className="grid gap-6 xl:grid-cols-[1.5fr_1fr]">
          <section className="rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
            <div className="flex items-center justify-between border-b border-[#e3e8e5] px-6 py-5">
              <div>
                <h2 className="text-base font-extrabold text-[#111827]">
                  Live operations
                </h2>

                <p className="mt-1 text-xs text-[#6b7280]">
                  Priority areas requiring administrator attention.
                </p>
              </div>

              <CheckCircle2
                size={20}
                className="text-[#397b0a]"
              />
            </div>

            <div className="divide-y divide-[#edf1ef]">
              <OperationRow
                icon={<FileCheck2 size={18} />}
                title="KYC verification queue"
                description="Review pending identity applications."
                value={data?.pendingKyc ?? 0}
                href="/kyc"
              />

              <OperationRow
                icon={<MessageSquare size={18} />}
                title="Client queries"
                description="Review requests requiring support attention."
                value={data?.openQueries ?? 0}
                href="/queries"
              />

              <OperationRow
                icon={<AlertTriangle size={18} />}
                title="Active P2P disputes"
                description="Monitor disputes requiring intervention."
                value={data?.activeDisputes ?? 0}
                href="/resolutions"
              />

              <OperationRow
                icon={<Clock3 size={18} />}
                title="Pending resolutions"
                description="Cases awaiting operational resolution."
                value={data?.pendingResolutions ?? 0}
                href="/resolutions"
              />
            </div>
          </section>

          {/* Admin status */}
          <section className="rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
            <div className="border-b border-[#e3e8e5] px-6 py-5">
              <h2 className="text-base font-extrabold text-[#111827]">
                Operations status
              </h2>

              <p className="mt-1 text-xs text-[#6b7280]">
                Current OakPay administration environment.
              </p>
            </div>

            <div className="space-y-4 p-6">
              <StatusRow
                label="Authentication"
                status="Operational"
              />

              <StatusRow
                label="KYC Management"
                status="Connected"
              />

              <StatusRow
                label="Client Queries"
                status="Connected"
              />

              <StatusRow
                label="Resolution Centre"
                status="Operational"
              />

              <StatusRow
                label="Admin API"
                status={error ? "Unavailable" : "Connected"}
                danger={Boolean(error)}
              />
            </div>
          </section>
        </div>

        {/* Quick actions */}
        <section>
          <div className="mb-4">
            <h2 className="text-base font-extrabold text-[#111827]">
              Quick actions
            </h2>

            <p className="mt-1 text-xs text-[#6b7280]">
              Jump directly into an operational workspace.
            </p>
          </div>

          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <QuickAction
              href="/kyc"
              title="Review KYC"
              description="Open the verification queue."
            />

            <QuickAction
              href="/queries"
              title="Client Queries"
              description="Review outstanding requests."
            />

            <QuickAction
              href="/resolutions"
              title="Resolution Centre"
              description="Review active cases."
            />

            <QuickAction
              href="/reports"
              title="View Reports"
              description="Open operational reporting."
            />
          </div>
        </section>
      </div>
    </AdminShell>
  );
}

function OperationRow({
  icon,
  title,
  description,
  value,
  href,
}: {
  icon: React.ReactNode;
  title: string;
  description: string;
  value: number;
  href: string;
}) {
  return (
    <a
      href={href}
      className="flex items-center justify-between gap-4 px-6 py-5 transition hover:bg-[#fafcfb]"
    >
      <div className="flex min-w-0 items-center gap-4">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[#eaf3e5] text-[#397b0a]">
          {icon}
        </div>

        <div className="min-w-0">
          <p className="text-sm font-bold text-[#111827]">
            {title}
          </p>

          <p className="mt-1 text-xs text-[#6b7280]">
            {description}
          </p>
        </div>
      </div>

      <span className="shrink-0 text-xl font-extrabold text-[#111827]">
        {value}
      </span>
    </a>
  );
}

function StatusRow({
  label,
  status,
  danger = false,
}: {
  label: string;
  status: string;
  danger?: boolean;
}) {
  return (
    <div className="flex items-center justify-between gap-4">
      <span className="text-sm font-medium text-[#374151]">
        {label}
      </span>

      <span
        className={[
          "inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-bold",
          danger
            ? "bg-red-50 text-red-700"
            : "bg-green-50 text-green-700",
        ].join(" ")}
      >
        <span
          className={[
            "h-1.5 w-1.5 rounded-full",
            danger ? "bg-red-500" : "bg-green-500",
          ].join(" ")}
        />

        {status}
      </span>
    </div>
  );
}

function QuickAction({
  href,
  title,
  description,
}: {
  href: string;
  title: string;
  description: string;
}) {
  return (
    <a
      href={href}
      className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-[#b8c8bc] hover:shadow-md"
    >
      <p className="text-sm font-extrabold text-[#111827]">
        {title}
      </p>

      <p className="mt-2 text-xs leading-5 text-[#6b7280]">
        {description}
      </p>

      <p className="mt-4 text-xs font-bold text-[#397b0a]">
        Open workspace →
      </p>
    </a>
  );
}