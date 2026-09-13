"use client";

import {
  BarChart3,
  FileCheck2,
  MessageSquare,
  Gavel,
  ArrowRight,
  Info,
} from "lucide-react";

import Link from "next/link";
import AdminShell from "../../components/AdminShell";

const reports = [
  {
    title: "KYC Report",
    description:
      "Review KYC application volumes, approvals, rejections and pending reviews.",
    href: "/kyc",
    icon: FileCheck2,
  },
  {
    title: "Client Queries",
    description:
      "Review operational client queries and their current resolution status.",
    href: "/queries",
    icon: MessageSquare,
  },
  {
    title: "Resolution Centre",
    description:
      "Review disputes, payment issues and other operational resolutions.",
    href: "/resolutions",
    icon: Gavel,
  },
];

export default function ReportsPage() {
  return (
    <AdminShell>
      <div className="space-y-7">

        {/* Page heading */}
        <div className="flex flex-col justify-between gap-4 lg:flex-row lg:items-end">
          <div>
            <div className="mb-2 flex items-center gap-3">
              <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#edf6e9]">
                <BarChart3 className="h-6 w-6 text-[#397b0a]" />
              </div>

              <div>
                <p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">
                  OakPay Operations
                </p>

                <h1 className="text-3xl font-extrabold tracking-tight text-[#111827]">
                  Reports
                </h1>
              </div>
            </div>

            <p className="ml-14 max-w-2xl text-sm text-[#6b7280]">
              OakPay operational reporting and performance overview.
            </p>
          </div>

          <div className="text-sm text-[#6b7280]">
            <Link href="/" className="hover:text-[#397b0a]">
              Home
            </Link>
            <span className="mx-2">›</span>
            <span className="font-semibold text-[#374151]">
              Reports
            </span>
          </div>
        </div>

        {/* Report cards */}
        <div className="grid gap-5 lg:grid-cols-3">
          {reports.map((report) => {
            const Icon = report.icon;

            return (
              <section
                key={report.title}
                className="group rounded-2xl border border-[#e3e8e5] bg-white p-7 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
              >
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#f1f7ef]">
                  <Icon className="h-7 w-7 text-[#397b0a]" />
                </div>

                <h2 className="mt-7 text-lg font-extrabold text-[#111827]">
                  {report.title}
                </h2>

                <p className="mt-3 min-h-[52px] text-sm leading-6 text-[#6b7280]">
                  {report.description}
                </p>

                <Link
                  href={report.href}
                  className="mt-7 inline-flex items-center gap-2 rounded-xl border border-[#b8d2a9] px-4 py-2.5 text-sm font-bold text-[#397b0a] transition hover:bg-[#f1f7ef]"
                >
                  Open report
                  <ArrowRight className="h-4 w-4" />
                </Link>
              </section>
            );
          })}
        </div>

        {/* Reporting status */}
        <section className="rounded-2xl border border-[#dce8d8] bg-[#f6faf4] p-6 shadow-sm">
          <div className="flex gap-4">
            <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-white">
              <Info className="h-5 w-5 text-[#397b0a]" />
            </div>

            <div>
              <h2 className="text-base font-extrabold text-[#111827]">
                Reporting status
              </h2>

              <p className="mt-2 max-w-4xl text-sm leading-6 text-[#6b7280]">
                The reporting area is available as an operational hub.
                Detailed report generation can be connected to the
                corresponding backend reporting endpoints as those
                endpoints are implemented.
              </p>
            </div>
          </div>
        </section>

      </div>
    </AdminShell>
  );
}