"use client";

import AdminShell from "../../components/AdminShell";

const rows = [
  {
    id: "#R-302",
    issue: "P2P payment dispute",
    reference: "#T-9182",
    priority: "Urgent",
    status: "Escalated",
  },
  {
    id: "#R-301",
    issue: "KYC rejection appeal",
    reference: "#K-4481",
    priority: "High",
    status: "In progress",
  },
  {
    id: "#R-300",
    issue: "Wallet balance mismatch",
    reference: "#W-7732",
    priority: "Normal",
    status: "Open",
  },
  {
    id: "#R-299",
    issue: "Payment marked but crypto not released",
    reference: "#T-9174",
    priority: "High",
    status: "In progress",
  },
];

function priorityClass(priority: string) {
  if (priority === "Urgent" || priority === "High") {
    return "bg-red-50 text-red-700";
  }

  return "bg-blue-50 text-blue-700";
}

function statusClass(status: string) {
  if (status === "Escalated") {
    return "bg-red-50 text-red-700";
  }

  if (status === "Resolved") {
    return "bg-green-50 text-green-700";
  }

  return "bg-amber-50 text-amber-700";
}

export default function Resolutions() {
  return (
    <AdminShell>
      <div className="space-y-8">
        {/* Page heading */}
        <div className="flex flex-col justify-between gap-3 md:flex-row md:items-end">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.12em] text-[#397b0a]">
              Operations & disputes
            </p>

            <h1 className="mt-1 text-2xl font-extrabold tracking-tight text-[#111827]">
              Resolution Centre
            </h1>

            <p className="mt-2 text-sm text-[#6b7280]">
              Resolve P2P, KYC and wallet issues.
            </p>
          </div>

          <div className="text-sm text-[#6b7280]">
            Operational case management
          </div>
        </div>

        {/* KPI cards */}
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">
              Open cases
            </p>

            <p className="mt-2 text-3xl font-extrabold text-[#111827]">
              6
            </p>

            <p className="mt-1 text-xs text-[#6b7280]">
              2 urgent
            </p>
          </div>

          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">
              Escalated
            </p>

            <p className="mt-2 text-3xl font-extrabold text-[#111827]">
              2
            </p>

            <p className="mt-1 text-xs text-[#6b7280]">
              Specialist review required
            </p>
          </div>

          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">
              Resolved today
            </p>

            <p className="mt-2 text-3xl font-extrabold text-[#111827]">
              14
            </p>

            <p className="mt-1 text-xs text-[#6b7280]">
              Across all case types
            </p>
          </div>

          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">
              SLA compliance
            </p>

            <p className="mt-2 text-3xl font-extrabold text-[#111827]">
              96%
            </p>

            <p className="mt-1 text-xs text-[#6b7280]">
              Current operations target
            </p>
          </div>
        </div>

        {/* Resolution table */}
        <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          <div className="flex flex-col justify-between gap-3 border-b border-[#e3e8e5] px-6 py-5 sm:flex-row sm:items-center">
            <div>
              <h2 className="text-base font-bold text-[#111827]">
                Active resolution cases
              </h2>

              <p className="mt-1 text-xs text-[#6b7280]">
                Cases requiring operational attention.
              </p>
            </div>

            <span className="inline-flex w-fit rounded-full bg-red-50 px-3 py-1 text-xs font-bold text-red-700">
              2 urgent
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-left">
              <thead className="bg-[#f8faf9]">
                <tr className="border-b border-[#e3e8e5]">
                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Case
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Issue
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Reference
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Priority
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Status
                  </th>

                  <th className="px-6 py-4 text-right text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Action
                  </th>
                </tr>
              </thead>

              <tbody>
                {rows.map((row) => (
                  <tr
                    key={row.id}
                    className="border-b border-[#edf1ef] last:border-b-0 hover:bg-[#fafcfb]"
                  >
                    <td className="whitespace-nowrap px-6 py-4 text-sm font-bold text-[#111827]">
                      {row.id}
                    </td>

                    <td className="px-6 py-4 text-sm text-[#374151]">
                      {row.issue}
                    </td>

                    <td className="whitespace-nowrap px-6 py-4 text-sm text-[#6b7280]">
                      {row.reference}
                    </td>

                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${priorityClass(
                          row.priority,
                        )}`}
                      >
                        {row.priority}
                      </span>
                    </td>

                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${statusClass(
                          row.status,
                        )}`}
                      >
                        {row.status}
                      </span>
                    </td>

                    <td className="px-6 py-4 text-right">
                      <button
                        type="button"
                        className="rounded-lg bg-[#145323] px-3 py-2 text-xs font-bold text-white transition hover:bg-[#0b3a1c]"
                      >
                        Open case
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        {/* Development/API notice */}
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-5 py-4">
          <p className="text-sm font-semibold text-amber-900">
            Resolution Centre backend integration
          </p>

          <p className="mt-1 text-xs leading-5 text-amber-800">
            The current cases shown here are the existing prototype records.
            We will connect this section to the actual P2P dispute and
            resolution backend once the corresponding admin API is confirmed.
          </p>
        </div>
      </div>
    </AdminShell>
  );
}