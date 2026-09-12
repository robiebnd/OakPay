"use client";

import Link from "next/link";
import Image from "next/image";
import {
  Activity,
  AlertTriangle,
  ArrowRight,
  BarChart3,
  Bell,
  CheckCircle2,
  ChevronDown,
  Clock3,
  FileCheck2,
  Gavel,
  LayoutDashboard,
  LogOut,
  MessageSquare,
  Search,
  Settings,
  ShieldCheck,
  Users,
  Wallet,
} from "lucide-react";

type IconType = React.ComponentType<{
  className?: string;
}>;

const navigation = [
  {
    label: "Dashboard",
    href: "/",
    icon: LayoutDashboard,
    active: true,
  },
  {
    label: "KYC Management",
    href: "/kyc",
    icon: ShieldCheck,
  },
  {
    label: "Client Queries",
    href: "/queries",
    icon: MessageSquare,
  },
  {
    label: "Resolution Centre",
    href: "/resolutions",
    icon: Gavel,
  },
  {
    label: "Wallet Issues",
    href: "/resolutions?type=wallet",
    icon: Wallet,
  },
  {
    label: "Reports",
    href: "/reports",
    icon: BarChart3,
  },
  {
    label: "Users & Access",
    href: "/users",
    icon: Users,
  },
  {
    label: "Settings",
    href: "/settings",
    icon: Settings,
  },
];

const metrics = [
  {
    title: "Pending KYC",
    value: "12",
    description: "Applications awaiting review",
    href: "/kyc",
    icon: Users,
    tone: "green",
  },
  {
    title: "Open Queries",
    value: "8",
    description: "Client queries requiring attention",
    href: "/queries",
    icon: MessageSquare,
    tone: "gold",
  },
  {
    title: "Active Disputes",
    value: "4",
    description: "P2P cases under investigation",
    href: "/resolutions",
    icon: AlertTriangle,
    tone: "orange",
  },
  {
    title: "Pending Resolutions",
    value: "6",
    description: "Cases awaiting action",
    href: "/resolutions",
    icon: BarChart3,
    tone: "green",
  },
];

const queues = [
  {
    title: "KYC Applications",
    description: "Identity verification applications waiting for review",
    count: 12,
    status: "Pending",
    href: "/kyc",
    icon: Users,
    tone: "green",
  },
  {
    title: "Client Queries",
    description: "Open client support requests",
    count: 8,
    status: "Open",
    href: "/queries",
    icon: MessageSquare,
    tone: "gold",
  },
  {
    title: "P2P Disputes",
    description: "Transactions requiring operational intervention",
    count: 4,
    status: "Active",
    href: "/resolutions",
    icon: AlertTriangle,
    tone: "orange",
  },
  {
    title: "Wallet Issues",
    description: "Wallet and balance-related cases",
    count: 3,
    status: "Review",
    href: "/resolutions?type=wallet",
    icon: Wallet,
    tone: "green",
  },
];

const activities = [
  {
    title: "KYC application submitted",
    description: "A new client submitted identity documents.",
    time: "12 minutes ago",
    icon: Users,
    tone: "green",
  },
  {
    title: "P2P dispute opened",
    description: "A payment dispute requires review.",
    time: "28 minutes ago",
    icon: AlertTriangle,
    tone: "orange",
  },
  {
    title: "Client query received",
    description: "A client reported a wallet issue.",
    time: "42 minutes ago",
    icon: MessageSquare,
    tone: "gold",
  },
  {
    title: "KYC application approved",
    description: "Identity verification was successfully completed.",
    time: "1 hour ago",
    icon: CheckCircle2,
    tone: "green",
  },
];

export default function AdminDashboard() {
  return (
    <div className="min-h-screen bg-[#f5f7f6]">

      {/* Sidebar */}
      <aside className="fixed inset-y-0 left-0 z-40 hidden w-[250px] bg-[#082d16] text-white lg:flex lg:flex-col">

        {/* Logo */}
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

        {/* Navigation */}
        <nav className="flex-1 px-3 py-5">

          <p className="mb-3 px-4 text-[10px] font-bold uppercase tracking-[0.18em] text-white/40">
            Operations
          </p>

          <div className="space-y-1.5">
            {navigation.map((item) => (
              <SidebarItem
                key={item.label}
                label={item.label}
                href={item.href}
                icon={item.icon}
                active={item.active}
              />
            ))}
          </div>

        </nav>

        {/* Administrator */}
        <div className="border-t border-white/10 p-4">

          <div className="flex items-center gap-3 rounded-xl p-2">

            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#397b0a] text-sm font-bold">
              RB
            </div>

            <div className="min-w-0">
              <p className="truncate text-sm font-semibold">
                Robson Banda
              </p>

              <p className="mt-0.5 text-xs text-white/50">
                Administrator
              </p>
            </div>

          </div>

          <button className="mt-3 flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium text-white/75 transition hover:bg-white/10 hover:text-white">
            <LogOut className="h-[18px] w-[18px]" />
            Logout
          </button>

        </div>
      </aside>

      {/* Main */}
      <div className="lg:pl-[250px]">

        {/* Top bar */}
        <header className="sticky top-0 z-30 flex h-[70px] items-center justify-between border-b border-[#e3e8e5] bg-white/95 px-5 backdrop-blur lg:px-8">

          {/* Search */}
          <div className="relative hidden w-full max-w-[500px] md:block">

            <Search className="absolute left-4 top-1/2 h-[18px] w-[18px] -translate-y-1/2 text-slate-400" />

            <input
              type="search"
              placeholder="Search users, transactions, queries, or reference numbers..."
              className="h-11 w-full rounded-full border-0 bg-[#f3f5f5] pl-11 pr-4 text-sm text-slate-700 outline-none placeholder:text-slate-400 focus:ring-2 focus:ring-[#397b0a]/20"
            />

          </div>

          {/* Right */}
          <div className="ml-auto flex items-center gap-4">

            <button className="relative flex h-10 w-10 items-center justify-center rounded-full transition hover:bg-slate-100">
              <Bell className="h-[19px] w-[19px] text-slate-600" />
              <span className="absolute right-2 top-1.5 h-2.5 w-2.5 rounded-full border-2 border-white bg-[#f5c400]" />
            </button>

            <div className="hidden h-7 w-px bg-slate-200 sm:block" />

            <button className="flex items-center gap-3 rounded-xl px-2 py-1.5 transition hover:bg-slate-50">

              <div className="flex h-9 w-9 items-center justify-center rounded-full bg-[#397b0a] text-xs font-bold text-white">
                RB
              </div>

              <div className="hidden text-left sm:block">
                <p className="text-sm font-semibold text-slate-800">
                  Robson Banda
                </p>

                <p className="text-xs text-slate-400">
                  Administrator
                </p>
              </div>

              <ChevronDown className="hidden h-4 w-4 text-slate-400 sm:block" />

            </button>

          </div>

        </header>

        {/* Content */}
        <main className="mx-auto max-w-[1600px] p-5 lg:p-8">

          {/* Page heading */}
          <div className="mb-7 flex flex-col justify-between gap-5 xl:flex-row xl:items-start">

            <div>
              <p className="mb-1.5 text-xs font-bold uppercase tracking-[0.16em] text-[#397b0a]">
                OakPay Operations
              </p>

              <h1 className="text-[38px] font-[650] leading-tight tracking-[-0.035em] text-slate-950">
                Overview
              </h1>

              <p className="mt-2 max-w-2xl text-[15px] text-slate-500">
                Monitor KYC, client support, P2P disputes and operational activity.
              </p>
            </div>

            <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white px-5 py-4 shadow-sm">

              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-50">
                <span className="h-3.5 w-3.5 rounded-full bg-emerald-500" />
              </div>

              <div>
                <p className="text-xs text-slate-400">
                  System status
                </p>

                <p className="mt-0.5 text-sm font-semibold text-slate-900">
                  Operational
                </p>

                <p className="text-[11px] text-slate-400">
                  All systems running normally
                </p>
              </div>

            </div>

          </div>

          {/* Metrics */}
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
              />
            ))}

          </section>

          {/* Queues + Activity */}
          <section className="mt-5 grid gap-5 xl:grid-cols-[1.45fr_1fr]">

            {/* Operational queues */}
            <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

              <PanelHeader
                title="Operational queues"
                description="Items that need administrator attention."
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
                  />
                ))}

              </div>

            </div>

            {/* Recent activity */}
            <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

              <PanelHeader
                title="Recent activity"
                description="Latest operational events."
                href="/reports"
              />

              <div className="divide-y divide-slate-100">

                {activities.map((activity) => (
                  <ActivityItem
                    key={activity.title}
                    title={activity.title}
                    description={activity.description}
                    time={activity.time}
                    icon={activity.icon}
                    tone={activity.tone}
                  />
                ))}

              </div>

            </div>

          </section>

          {/* Quick Actions */}
          <section className="mt-5 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">

            <div className="mb-5">
              <h2 className="text-lg font-[650] tracking-[-0.02em] text-slate-900">
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
                icon={Users}
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
                icon={Gavel}
                tone="orange"
              />

            </div>

          </section>

          {/* Footer */}
          <footer className="flex flex-col justify-between gap-3 px-1 pb-2 pt-6 text-xs text-slate-400 sm:flex-row">

            <p>
              © {new Date().getFullYear()} OakPay. All rights reserved.
            </p>

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
}: {
  title: string;
  value: string;
  description: string;
  href: string;
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
      className="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition duration-200 hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="flex items-start justify-between">

        <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${styles[tone]}`}>
          <Icon className="h-6 w-6" />
        </div>

        <div className="flex h-9 w-9 items-center justify-center rounded-full bg-slate-50 transition group-hover:bg-emerald-50">
          <ArrowRight className="h-4 w-4 text-slate-400 transition group-hover:translate-x-0.5 group-hover:text-emerald-700" />
        </div>

      </div>

      <div className="mt-5">
        <p className="text-sm font-medium text-slate-500">
          {title}
        </p>

        <p className="mt-1 text-[34px] font-[650] leading-none tracking-[-0.04em] text-slate-950">
          {value}
        </p>

        <p className="mt-2 text-xs text-slate-400">
          {description}
        </p>
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

        <p className="mt-1 text-sm text-slate-500">
          {description}
        </p>
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
}: {
  title: string;
  description: string;
  count: number;
  status: string;
  href: string;
  icon: IconType;
  tone: "green" | "gold" | "orange";
}) {
  const iconStyles = {
    green: "bg-emerald-50 text-emerald-700",
    gold: "bg-amber-50 text-amber-700",
    orange: "bg-orange-50 text-orange-700",
  };

  const statusStyles = {
    Pending: "bg-amber-50 text-amber-700",
    Open: "bg-emerald-50 text-emerald-700",
    Active: "bg-amber-50 text-amber-700",
    Review: "bg-blue-50 text-blue-700",
  };

  return (
    <Link
      href={href}
      className="group flex items-center gap-4 px-5 py-4 transition hover:bg-slate-50"
    >
      <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${iconStyles[tone]}`}>
        <Icon className="h-5 w-5" />
      </div>

      <div className="min-w-0 flex-1">
        <p className="text-sm font-semibold text-slate-800">
          {title}
        </p>

        <p className="mt-1 truncate text-xs text-slate-400">
          {description}
        </p>
      </div>

      <div className="hidden items-center gap-5 sm:flex">

        <span className="text-lg font-[650] text-slate-900">
          {count}
        </span>

        <span className={`rounded-full px-3 py-1 text-[11px] font-semibold ${statusStyles[status as keyof typeof statusStyles]}`}>
          {status}
        </span>

        <ArrowRight className="h-4 w-4 text-slate-300 transition group-hover:translate-x-1 group-hover:text-emerald-700" />

      </div>

    </Link>
  );
}

function ActivityItem({
  title,
  description,
  time,
  icon: Icon,
  tone,
}: {
  title: string;
  description: string;
  time: string;
  icon: IconType;
  tone: "green" | "gold" | "orange";
}) {
  const styles = {
    green: "bg-emerald-50 text-emerald-700",
    gold: "bg-amber-50 text-amber-700",
    orange: "bg-orange-50 text-orange-700",
  };

  return (
    <div className="flex gap-3 px-5 py-4">

      <div className={`mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-xl ${styles[tone]}`}>
        <Icon className="h-[18px] w-[18px]" />
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex items-start justify-between gap-3">

          <p className="text-sm font-semibold text-slate-800">
            {title}
          </p>

          <span className="shrink-0 text-[11px] text-slate-400">
            {time}
          </span>

        </div>

        <p className="mt-1 text-xs leading-5 text-slate-400">
          {description}
        </p>
      </div>

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
      <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${styles[tone]}`}>
        <Icon className="h-5 w-5" />
      </div>

      <div className="min-w-0 flex-1">
        <p className="text-sm font-semibold text-slate-800">
          {title}
        </p>

        <p className="mt-1 truncate text-xs text-slate-400">
          {description}
        </p>
      </div>

      <ArrowRight className="h-4 w-4 shrink-0 text-slate-300 transition group-hover:translate-x-1 group-hover:text-emerald-700" />
    </Link>
  );
}
