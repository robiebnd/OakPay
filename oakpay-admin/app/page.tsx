"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { useAdminDashboard } from "../hooks/useAdminDashboard";
import {
  AlertTriangle,
  ArrowRight,
  BarChart3,
  Bell,
  CheckCircle2,
  ChevronDown,
  LayoutDashboard,
  LogOut,
  MessageSquare,
  RefreshCw,
  Search,
  Settings,
  ShieldCheck,
  Users,
  Wallet,
} from "lucide-react";

type IconType = React.ComponentType<{ className?: string }>;

const navigation = [
  { label: "Dashboard", href: "/", icon: LayoutDashboard },
  { label: "KYC Management", href: "/kyc", icon: ShieldCheck },
  { label: "Client Queries", href: "/queries", icon: MessageSquare },
  { label: "Resolution Centre", href: "/resolutions", icon: AlertTriangle },
  { label: "Wallet Issues", href: "/resolutions?type=wallet", icon: Wallet },
  { label: "Reports", href: "/reports", icon: BarChart3 },
  { label: "Users & Access", href: "/users", icon: Users },
  { label: "Settings", href: "/settings", icon: Settings },
];

export default function AdminDashboard() {
  const pathname = usePathname();
  const router = useRouter();
  const { data, loading, error, reload } = useAdminDashboard();
  const [search, setSearch] = useState("");
  const [loggingOut, setLoggingOut] = useState(false);

  const metrics = [
    {
      title: "Pending KYC",
      value: data?.pendingKyc ?? 0,
      description: "Applications awaiting review",
      href: "/kyc",
      icon: ShieldCheck,
      tone: "green" as const,
    },
    {
      title: "Open Queries",
      value: data?.openQueries ?? 0,
      description: "Client queries requiring attention",
      href: "/queries",
      icon: MessageSquare,
      tone: "gold" as const,
    },
    {
      title: "Active Disputes",
      value: data?.activeDisputes ?? 0,
      description: "P2P cases requiring intervention",
      href: "/resolutions",
      icon: AlertTriangle,
      tone: "orange" as const,
    },
    {
      title: "Pending Resolutions",
      value: data?.pendingResolutions ?? 0,
      description: "Cases awaiting operational action",
      href: "/resolutions",
      icon: CheckCircle2,
      tone: "green" as const,
    },
  ];

  const queues = [
    {
      title: "KYC Applications",
      description: "Identity verification applications waiting for review",
      count: data?.pendingKyc ?? 0,
      status: "Pending",
      href: "/kyc",
      icon: ShieldCheck,
      tone: "green" as const,
    },
    {
      title: "Client Queries",
      description: "Open client support requests",
      count: data?.openQueries ?? 0,
      status: "Open",
      href: "/queries",
      icon: MessageSquare,
      tone: "gold" as const,
    },
    {
      title: "P2P Disputes",
      description: "Transactions requiring operational intervention",
      count: data?.activeDisputes ?? 0,
      status: "Active",
      href: "/resolutions",
      icon: AlertTriangle,
      tone: "orange" as const,
    },
    {
      title: "Pending Resolutions",
      description: "Cases waiting for an administrator decision",
      count: data?.pendingResolutions ?? 0,
      status: "Review",
      href: "/resolutions",
      icon: CheckCircle2,
      tone: "green" as const,
    },
  ];

  const totalAttention =
    (data?.pendingKyc ?? 0) +
    (data?.openQueries ?? 0) +
    (data?.activeDisputes ?? 0) +
    (data?.pendingResolutions ?? 0);

  function handleSearchSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const term = search.trim();
    if (!term) return;

    router.push(`/users?query=${encodeURIComponent(term)}`);
  }

  function handleLogout() {
    setLoggingOut(true);

    if (typeof window !== "undefined") {
      localStorage.removeItem("oakpay.accessToken");
      localStorage.removeItem("oakpay.refreshToken");
      localStorage.removeItem("oakpay.user");
    }

    router.replace("/login");
  }

  return (
    <div className="min-h-screen bg-[#f5f7f6]">
      <aside className="fixed inset-y-0 left-0 z-40 hidden w-[250px] bg-[#082d16] text-white lg:flex lg:flex-col">
        <div className="flex h-[82px] items-center px-7">
          <Link href="/" className="relative block h-[46px] w-[170px]">
            <Image
              src="/oakpay-logo.png"
              alt="OakPay"
              fill
              priority
              className="object-contain object-left"
            />
          </Link>
        </div>

        <nav className="flex-1 overflow-y-auto px-3 py-5">
          <p className="mb-3 px-4 text-[10px] font-bold uppercase tracking-[0.18em] text-white/40">
            Operations
          </p>

          <div className="space-y-1.5">
            {navigation.map((item) => {
              const active =
                item.href === "/"
                  ? pathname === "/"
                  : pathname === item.href ||
                    pathname.startsWith(`${item.href.split("?")[0]}/`);

              return (
                <SidebarItem
                  key={item.label}
                  label={item.label}
                  href={item.href}
                  icon={item.icon}
                  active={active}
                />
              );
            })}
          </div>
        </nav>

        <div className="border-t border-white/10 p-4">
          <div className="flex items-center gap-3 rounded-xl p-2">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#397b0a] text-sm font-bold">
              RB
            </div>

            <div className="min-w-0">
              <p className="truncate text-sm font-semibold">Robson Banda</p>
              <p className="mt-0.5 text-xs text-white/50">Administrator</p>
            </div>
          </div>

          <button
            type="button"
            onClick={handleLogout}
            disabled={loggingOut}
            className="mt-3 flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium text-white/75 transition hover:bg-white/10 hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
          >
            <LogOut className="h-[18px] w-[18px]" />
            {loggingOut ? "Logging out..." : "Logout"}
          </button>
        </div>
      </aside>

      <div className="lg:pl-[250px]">
        <header className="sticky top-0 z-30 flex h-[70px] items-center justify-between border-b border-[#e3e8e5] bg-white/95 px-5 backdrop-blur lg:px-8">
          <form
            onSubmit={handleSearchSubmit}
            className="relative hidden w-full max-w-[520px] md:block"
          >
            <Search className="absolute left-4 top-1/2 h-[18px] w-[18px] -translate-y-1/2 text-slate-400" />
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search users, transactions, queries, or reference numbers..."
              aria-label="Search OakPay administration"
              className="h-11 w-full rounded-full border-0 bg-[#f3f5f5] pl-11 pr-4 text-sm text-slate-700 outline-none placeholder:text-slate-400 focus:ring-2 focus:ring-[#397b0a]/20"
            />
          </form>

          <div className="ml-auto flex items-center gap-4">
            <button
              type="button"
              onClick={() => void reload()}
              disabled={loading}
              title="Refresh dashboard"
              aria-label="Refresh dashboard"
              className="flex h-10 w-10 items-center justify-center rounded-full transition hover:bg-slate-100 disabled:opacity-50"
            >
              <RefreshCw
                className={`h-[18px] w-[18px] text-slate-600 ${
                  loading ? "animate-spin" : ""
                }`}
              />
            </button>

            <button
              type="button"
              aria-label="Notifications"
              className="relative flex h-10 w-10 items-center justify-center rounded-full transition hover:bg-slate-100"
            >
              <Bell className="h-[19px] w-[19px] text-slate-600" />
              {totalAttention > 0 && (
                <span className="absolute right-2 top-1.5 h-2.5 w-2.5 rounded-full border-2 border-white bg-[#f5c400]" />
              )}
            </button>

            <div className="hidden h-7 w-px bg-slate-200 sm:block" />

            <button
              type="button"
              className="flex items-center gap-3 rounded-xl px-2 py-1.5 transition hover:bg-slate-50"
            >
              <div className="flex h-9 w-9 items-center justify-center rounded-full bg-[#397b0a] text-xs font-bold text-white">
                RB
              </div>

              <div className="hidden text-left sm:block">
                <p className="text-sm font-semibold text-slate-800">
                  Robson Banda
                </p>
                <p className="text-xs text-slate-400">Administrator</p>
              </div>

              <ChevronDown className="hidden h-4 w-4 text-slate-400 sm:block" />
            </button>
          </div>
        </header>

        <main className="mx-auto max-w-[1600px] p-5 lg:p-8">
          <div className="mb-7 flex flex-col justify-between gap-5 xl:flex-row xl:items-start">
            <div>
              <p className="mb-1.5 text-xs font-bold uppercase tracking-[0.16em] text-[#397b0a]">
                OakPay Operations
              </p>

              <h1 className="text-[38px] font-[650] leading-tight tracking-[-0.035em] text-slate-950">
                Overview
              </h1>

              <p className="mt-2 max-w-2xl text-[15px] text-slate-500">
                Monitor KYC, client support, P2P disputes and operational
                activity from one workspace.
              </p>
            </div>

            <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white px-5 py-4 shadow-sm">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-50">
                <span className="h-3.5 w-3.5 rounded-full bg-emerald-500" />
              </div>

              <div>
                <p className="text-xs text-slate-400">System status</p>
                <p className="mt-0.5 text-sm font-semibold text-slate-900">
                  Operational
                </p>
                <p className="text-[11px] text-slate-400">
                  Dashboard connected to the admin API
                </p>
              </div>
            </div>
          </div>

          {error && (
            <div className="mb-5 flex flex-col gap-3 rounded-2xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p className="text-sm font-semibold text-red-800">
                  Dashboard data could not be loaded.
                </p>
                <p className="mt-1 text-xs text-red-600">
                  {error}
                </p>
              </div>

              <button
                type="button"
                onClick={() => void reload()}
                className="inline-flex items-center justify-center gap-2 rounded-xl bg-red-700 px-4 py-2.5 text-xs font-semibold text-white transition hover:bg-red-800"
              >
                <RefreshCw className="h-3.5 w-3.5" />
                Retry
              </button>
            </div>
          )}

          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {metrics.map((metric) => (
              <MetricCard
                key={metric.title}
                title={metric.title}
                value={metric.value}
                description={metric.description}
                href={metric.href}
                icon={metric.icon}
                tone={metric.tone}
                loading={loading}
              />
            ))}
          </section>

          <section className="mt-5 grid gap-5 xl:grid-cols-[1.45fr_1fr]">
            <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <PanelHeader
                title="Operational queues"
                description="Live queues that need administrator attention."
                href="/kyc"
              />

              <div className="divide-y divide-slate-100">
                {queues.map((queue) => (
                  <QueueItem
                    key={queue.title}
                    title={queue.title}
                    description={queue.description}
                    count={queue.count}
                    status={queue.status}
                    href={queue.href}
                    icon={queue.icon}
                    tone={queue.tone}
                    loading={loading}
                  />
                ))}
              </div>
            </div>

            <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <div className="border-b border-slate-100 px-5 py-5">
                <h2 className="text-lg font-[650] tracking-[-0.025em] text-slate-900">
                  Administration status
                </h2>
                <p className="mt-1 text-sm text-slate-500">
                  Current workload across the OakPay operations centre.
                </p>
              </div>

              <div className="space-y-4 p-5">
                <StatusRow
                  label="Items requiring attention"
                  value={loading ? "—" : String(totalAttention)}
                  icon={AlertTriangle}
                />
                <StatusRow
                  label="KYC queue"
                  value={loading ? "—" : String(data?.pendingKyc ?? 0)}
                  icon={ShieldCheck}
                />
                <StatusRow
                  label="Support queue"
                  value={loading ? "—" : String(data?.openQueries ?? 0)}
                  icon={MessageSquare}
                />
                <StatusRow
                  label="Resolution queue"
                  value={loading ? "—" : String(data?.pendingResolutions ?? 0)}
                  icon={CheckCircle2}
                />

                <Link
                  href="/reports"
                  className="mt-2 flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-emerald-50 hover:text-emerald-700"
                >
                  Open operational reports
                  <ArrowRight className="h-4 w-4" />
                </Link>
              </div>
            </div>
          </section>

          <section className="mt-5 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="mb-5">
              <h2 className="text-lg font-[650] tracking-[-0.02em] text-slate-900">
                Quick actions
              </h2>
              <p className="mt-1 text-sm text-slate-500">
                Go directly to the operational workspace you need.
              </p>
            </div>

            <div className="grid gap-3 md:grid-cols-3">
              <QuickAction
                href="/kyc"
                title="Review KYC"
                description="Review pending identity applications"
                icon={ShieldCheck}
                tone="green"
              />
              <QuickAction
                href="/queries"
                title="Client Queries"
                description="Respond to client support requests"
                icon={MessageSquare}
                tone="gold"
              />
              <QuickAction
                href="/resolutions"
                title="Resolution Centre"
                description="Handle disputes and operational cases"
                icon={AlertTriangle}
                tone="orange"
              />
            </div>
          </section>

          <footer className="flex flex-col justify-between gap-3 px-1 pb-2 pt-6 text-xs text-slate-400 sm:flex-row">
            <p>© {new Date().getFullYear()} OakPay. All rights reserved.</p>

            <div className="flex gap-5">
              <Link href="/privacy" className="hover:text-slate-600">
                Privacy
              </Link>
              <Link href="/terms" className="hover:text-slate-600">
                Terms
              </Link>
              <Link href="/support" className="hover:text-slate-600">
                Support
              </Link>
            </div>
          </footer>
        </main>
      </div>
    </div>
  );
}

function SidebarItem({
  label,
  href,
  icon: Icon,
  active = false,
}: {
  label: string;
  href: string;
  icon: IconType;
  active?: boolean;
}) {
  return (
    <Link
      href={href}
      className={`group flex items-center gap-3 rounded-xl px-4 py-3 text-[14px] font-medium transition ${
        active
          ? "bg-[#397b0a]/35 text-white shadow-inner"
          : "text-white/70 hover:bg-white/10 hover:text-white"
      }`}
    >
      <Icon
        className={`h-[19px] w-[19px] shrink-0 ${
          active ? "text-[#f5c400]" : "text-white/80"
        }`}
      />
      <span>{label}</span>
    </Link>
  );
}

function MetricCard({
  title,
  value,
  description,
  href,
  icon: Icon,
  tone,
  loading,
}: {
  title: string;
  value: number;
  description: string;
  href: string;
  icon: IconType;
  tone: "green" | "gold" | "orange";
  loading: boolean;
}) {
  const styles = {
    green: "bg-emerald-50 text-emerald-700",
    gold: "bg-amber-50 text-amber-700",
    orange: "bg-orange-50 text-orange-700",
  };

  return (
    <Link
      href={href}
      className="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition duration-200 hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="flex items-start justify-between">
        <div
          className={`flex h-12 w-12 items-center justify-center rounded-xl ${styles[tone]}`}
        >
          <Icon className="h-6 w-6" />
        </div>

        <div className="flex h-9 w-9 items-center justify-center rounded-full bg-slate-50 transition group-hover:bg-emerald-50">
          <ArrowRight className="h-4 w-4 text-slate-400 transition group-hover:translate-x-0.5 group-hover:text-emerald-700" />
        </div>
      </div>

      <div className="mt-5">
        <p className="text-sm font-medium text-slate-500">{title}</p>

        <p className="mt-1 text-[34px] font-[650] leading-none tracking-[-0.04em] text-slate-950">
          {loading ? (
            <span className="inline-block h-9 w-12 animate-pulse rounded-md bg-slate-100" />
          ) : (
            value
          )}
        </p>

        <p className="mt-2 text-xs text-slate-400">{description}</p>
      </div>
    </Link>
  );
}

function PanelHeader({
  title,
  description,
  href,
}: {
  title: string;
  description: string;
  href: string;
}) {
  return (
    <div className="flex items-center justify-between border-b border-slate-100 px-5 py-5">
      <div>
        <h2 className="text-lg font-[650] tracking-[-0.025em] text-slate-900">
          {title}
        </h2>
        <p className="mt-1 text-sm text-slate-500">{description}</p>
      </div>

      <Link
        href={href}
        className="hidden items-center gap-2 rounded-full bg-slate-50 px-4 py-2 text-xs font-semibold text-slate-700 transition hover:bg-emerald-50 hover:text-emerald-700 sm:flex"
      >
        View all
        <ArrowRight className="h-3.5 w-3.5" />
      </Link>
    </div>
  );
}

function QueueItem({
  title,
  description,
  count,
  status,
  href,
  icon: Icon,
  tone,
  loading,
}: {
  title: string;
  description: string;
  count: number;
  status: string;
  href: string;
  icon: IconType;
  tone: "green" | "gold" | "orange";
  loading: boolean;
}) {
  const iconStyles = {
    green: "bg-emerald-50 text-emerald-700",
    gold: "bg-amber-50 text-amber-700",
    orange: "bg-orange-50 text-orange-700",
  };

  const statusStyles = {
    Pending: "bg-amber-50 text-amber-700",
    Open: "bg-emerald-50 text-emerald-700",
    Active: "bg-orange-50 text-orange-700",
    Review: "bg-blue-50 text-blue-700",
  };

  return (
    <Link
      href={href}
      className="group flex items-center gap-4 px-5 py-4 transition hover:bg-slate-50"
    >
      <div
        className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${iconStyles[tone]}`}
      >
        <Icon className="h-5 w-5" />
      </div>

      <div className="min-w-0 flex-1">
        <p className="text-sm font-semibold text-slate-800">{title}</p>
        <p className="mt-1 truncate text-xs text-slate-400">{description}</p>
      </div>

      <div className="hidden items-center gap-5 sm:flex">
        {loading ? (
          <span className="h-6 w-8 animate-pulse rounded bg-slate-100" />
        ) : (
          <span className="text-lg font-[650] text-slate-900">{count}</span>
        )}

        <span
          className={`rounded-full px-3 py-1 text-[11px] font-semibold ${
            statusStyles[status as keyof typeof statusStyles]
          }`}
        >
          {status}
        </span>

        <ArrowRight className="h-4 w-4 text-slate-300 transition group-hover:translate-x-1 group-hover:text-emerald-700" />
      </div>
    </Link>
  );
}

function StatusRow({
  label,
  value,
  icon: Icon,
}: {
  label: string;
  value: string;
  icon: IconType;
}) {
  return (
    <div className="flex items-center justify-between rounded-xl border border-slate-100 px-4 py-3">
      <div className="flex items-center gap-3">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-50">
          <Icon className="h-4 w-4 text-slate-500" />
        </div>
        <span className="text-sm font-medium text-slate-700">{label}</span>
      </div>

      <span className="text-lg font-[650] text-slate-950">{value}</span>
    </div>
  );
}

function QuickAction({
  href,
  title,
  description,
  icon: Icon,
  tone,
}: {
  href: string;
  title: string;
  description: string;
  icon: IconType;
  tone: "green" | "gold" | "orange";
}) {
  const styles = {
    green: "bg-emerald-50 text-emerald-700",
    gold: "bg-amber-50 text-amber-700",
    orange: "bg-orange-50 text-orange-700",
  };

  return (
    <Link
      href={href}
      className="group flex items-center gap-4 rounded-xl border border-slate-200 p-4 transition hover:border-emerald-200 hover:bg-emerald-50/30"
    >
      <div
        className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${styles[tone]}`}
      >
        <Icon className="h-5 w-5" />
      </div>

      <div className="min-w-0 flex-1">
        <p className="text-sm font-semibold text-slate-800">{title}</p>
        <p className="mt-1 truncate text-xs text-slate-400">{description}</p>
      </div>

      <ArrowRight className="h-4 w-4 shrink-0 text-slate-300 transition group-hover:translate-x-1 group-hover:text-emerald-700" />
    </Link>
  );
}
