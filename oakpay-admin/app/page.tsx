"use client";

import Link from "next/link";
import {
  Users,
  ShieldCheck,
  MessageSquare,
  AlertTriangle,
  Wallet,
  Activity,
  ArrowRight,
  Clock3,
  CheckCircle2,
  XCircle,
} from "lucide-react";

const stats = [
  {
    title: "Pending KYC",
    value: "12",
    description: "Applications awaiting review",
    icon: ShieldCheck,
    href: "/kyc",
  },
  {
    title: "Open Queries",
    value: "8",
    description: "Client queries requiring attention",
    icon: MessageSquare,
    href: "/queries",
  },
  {
    title: "Active Disputes",
    value: "4",
    description: "P2P cases under investigation",
    icon: AlertTriangle,
    href: "/resolutions",
  },
  {
    title: "Pending Resolutions",
    value: "6",
    description: "Cases awaiting action",
    icon: Activity,
    href: "/resolutions",
  },
];

const recentActivity = [
  {
    title: "KYC application submitted",
    description: "A new client submitted identity documents.",
    time: "12 minutes ago",
    type: "kyc",
  },
  {
    title: "P2P dispute opened",
    description: "A payment dispute requires review.",
    time: "28 minutes ago",
    type: "dispute",
  },
  {
    title: "Client query received",
    description: "A client reported a wallet issue.",
    time: "42 minutes ago",
    type: "query",
  },
  {
    title: "KYC application approved",
    description: "Identity verification was successfully completed.",
    time: "1 hour ago",
    type: "success",
  },
];

export default function AdminDashboard() {
  return (
    <main className="min-h-screen bg-slate-50">
      <div className="mx-auto max-w-[1600px] p-6 lg:p-8">

        {/* Header */}
        <div className="mb-8 flex flex-col justify-between gap-4 lg:flex-row lg:items-center">
          <div>
            <p className="mb-1 text-sm font-medium text-emerald-700">
              OakPay Operations
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-slate-900">
              Overview
            </h1>

            <p className="mt-2 text-sm text-slate-500">
              Monitor KYC, client support, P2P disputes and operational activity.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <div className="rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-sm">
              <p className="text-xs text-slate-400">System status</p>

              <div className="mt-1 flex items-center gap-2">
                <span className="h-2.5 w-2.5 rounded-full bg-emerald-500" />
                <span className="text-sm font-semibold text-slate-700">
                  Operational
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* KPI cards */}
        <section className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
          {stats.map((stat) => {
            const Icon = stat.icon;

            return (
              <Link
                key={stat.title}
                href={stat.href}
                className="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
              >
                <div className="flex items-start justify-between">
                  <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-emerald-50">
                    <Icon className="h-5 w-5 text-emerald-700" />
                  </div>

                  <ArrowRight className="h-4 w-4 text-slate-300 transition group-hover:translate-x-1 group-hover:text-emerald-600" />
                </div>

                <div className="mt-5">
                  <p className="text-sm font-medium text-slate-500">
                    {stat.title}
                  </p>

                  <p className="mt-1 text-3xl font-bold text-slate-900">
                    {stat.value}
                  </p>

                  <p className="mt-2 text-xs text-slate-400">
                    {stat.description}
                  </p>
                </div>
              </Link>
            );
          })}
        </section>

        {/* Main content */}
        <section className="mt-8 grid gap-6 xl:grid-cols-[1.5fr_1fr]">

          {/* Operational queues */}
          <div className="rounded-2xl border border-slate-200 bg-white shadow-sm">

            <div className="flex items-center justify-between border-b border-slate-100 p-6">
              <div>
                <h2 className="font-semibold text-slate-900">
                  Operational queues
                </h2>

                <p className="mt-1 text-sm text-slate-500">
                  Items that need administrator attention.
                </p>
              </div>
            </div>

            <div className="divide-y divide-slate-100">

              <QueueRow
                icon={<ShieldCheck className="h-5 w-5" />}
                title="KYC Applications"
                description="Identity verification applications waiting for review"
                count="12"
                href="/kyc"
                status="Pending"
              />

              <QueueRow
                icon={<MessageSquare className="h-5 w-5" />}
                title="Client Queries"
                description="Open client support requests"
                count="8"
                href="/queries"
                status="Open"
              />

              <QueueRow
                icon={<AlertTriangle className="h-5 w-5" />}
                title="P2P Disputes"
                description="Transactions requiring operational intervention"
                count="4"
                href="/resolutions"
                status="Active"
              />

              <QueueRow
                icon={<Wallet className="h-5 w-5" />}
                title="Wallet Issues"
                description="Wallet and balance-related cases"
                count="3"
                href="/resolutions"
                status="Review"
              />

            </div>
          </div>

          {/* Recent activity */}
          <div className="rounded-2xl border border-slate-200 bg-white shadow-sm">

            <div className="border-b border-slate-100 p-6">
              <h2 className="font-semibold text-slate-900">
                Recent activity
              </h2>

              <p className="mt-1 text-sm text-slate-500">
                Latest operational events.
              </p>
            </div>

            <div className="divide-y divide-slate-100">
              {recentActivity.map((activity, index) => (
                <div key={index} className="p-5">
                  <div className="flex gap-3">

                    <div className="mt-1">
                      {activity.type === "success" ? (
                        <CheckCircle2 className="h-5 w-5 text-emerald-600" />
                      ) : activity.type === "dispute" ? (
                        <AlertTriangle className="h-5 w-5 text-amber-500" />
                      ) : activity.type === "query" ? (
                        <MessageSquare className="h-5 w-5 text-blue-500" />
                      ) : (
                        <ShieldCheck className="h-5 w-5 text-emerald-600" />
                      )}
                    </div>

                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-semibold text-slate-800">
                        {activity.title}
                      </p>

                      <p className="mt-1 text-xs leading-5 text-slate-500">
                        {activity.description}
                      </p>

                      <div className="mt-2 flex items-center gap-1.5 text-xs text-slate-400">
                        <Clock3 className="h-3.5 w-3.5" />
                        {activity.time}
                      </div>
                    </div>

                  </div>
                </div>
              ))}
            </div>

          </div>
        </section>

        {/* Quick actions */}
        <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-5">
            <h2 className="font-semibold text-slate-900">
              Quick actions
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Jump directly into the areas requiring attention.
            </p>
          </div>

          <div className="grid gap-3 md:grid-cols-3">

            <QuickAction
              href="/kyc"
              title="Review KYC"
              description="Review pending identity applications"
              icon={<ShieldCheck />}
            />

            <QuickAction
              href="/queries"
              title="Client Queries"
              description="Respond to client support requests"
              icon={<MessageSquare />}
            />

            <QuickAction
              href="/resolutions"
              title="Resolution Centre"
              description="Handle disputes and operational cases"
              icon={<AlertTriangle />}
            />

          </div>
        </section>

      </div>
    </main>
  );
}

function QueueRow({
  icon,
  title,
  description,
  count,
  href,
  status,
}: {
  icon: React.ReactNode;
  title: string;
  description: string;
  count: string;
  href: string;
  status: string;
}) {
  return (
    <Link
      href={href}
      className="group flex items-center gap-4 p-5 transition hover:bg-slate-50"
    >
      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-slate-100 text-slate-600 group-hover:bg-emerald-50 group-hover:text-emerald-700">
        {icon}
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2">
          <h3 className="text-sm font-semibold text-slate-800">
            {title}
          </h3>

          <span className="rounded-full bg-amber-50 px-2 py-0.5 text-[10px] font-semibold text-amber-700">
            {status}
          </span>
        </div>

        <p className="mt-1 truncate text-xs text-slate-400">
          {description}
        </p>
      </div>

      <div className="text-right">
        <p className="text-xl font-bold text-slate-900">
          {count}
        </p>

        <ArrowRight className="ml-auto mt-1 h-4 w-4 text-slate-300 group-hover:text-emerald-600" />
      </div>
    </Link>
  );
}

function QuickAction({
  href,
  title,
  description,
  icon,
}: {
  href: string;
  title: string;
  description: string;
  icon: React.ReactNode;
}) {
  return (
    <Link
      href={href}
      className="group flex items-center gap-4 rounded-xl border border-slate-200 p-4 transition hover:border-emerald-200 hover:bg-emerald-50/40"
    >
      <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-100 text-slate-600 group-hover:bg-emerald-100 group-hover:text-emerald-700">
        {icon}
      </div>

      <div className="flex-1">
        <p className="text-sm font-semibold text-slate-800">
          {title}
        </p>

        <p className="mt-1 text-xs text-slate-400">
          {description}
        </p>
      </div>

      <ArrowRight className="h-4 w-4 text-slate-300 group-hover:text-emerald-600" />
    </Link>
  );
}
