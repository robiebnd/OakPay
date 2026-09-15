"use client";

import { useEffect, useMemo, useState } from "react";
import {
  CheckCircle2,
  Clock3,
  Filter,
  MessageSquare,
  Search,
  ShieldAlert,
  UserRound,
  X,
} from "lucide-react";

import AdminShell from "../../components/AdminShell";
import { adminApi, ClientQuery } from "../../lib/adminApi";

type QueryFilter =
  | "ALL"
  | "OPEN"
  | "ASSIGNED"
  | "ESCALATED"
  | "RESOLVED";

export default function Queries() {
  const [queries, setQueries] = useState<ClientQuery[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<QueryFilter>("ALL");

  const [selected, setSelected] = useState<ClientQuery | null>(null);
  const [resolution, setResolution] = useState("");
  const [busy, setBusy] = useState(false);

  async function loadQueries() {
    try {
      setLoading(true);
      setError("");

      const result = await adminApi.queries.list(
        status === "ALL" ? undefined : status,
      );

      setQueries(result);
    } catch (err) {
      if (
        err instanceof Error &&
        err.message === "ADMIN_AUTH_REQUIRED"
      ) {
        window.location.href = "/login";
        return;
      }

      setError(
        err instanceof Error
          ? err.message
          : "Unable to load client queries.",
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadQueries();
  }, [status]);

  const filteredQueries = useMemo(() => {
    const term = search.trim().toLowerCase();

    if (!term) {
      return queries;
    }

    return queries.filter((query) =>
      [
        query.id,
        query.userId,
        query.subject,
        query.category,
        query.priority,
        query.status,
        query.description,
        query.resolution,
      ]
        .filter(Boolean)
        .some((value) =>
          String(value).toLowerCase().includes(term),
        ),
    );
  }, [queries, search]);

  const counts = useMemo(() => {
    return {
      total: queries.length,
      open: queries.filter((q) => q.status === "OPEN").length,
      assigned: queries.filter((q) => q.status === "ASSIGNED").length,
      escalated: queries.filter((q) => q.status === "ESCALATED").length,
      resolved: queries.filter((q) => q.status === "RESOLVED").length,
    };
  }, [queries]);

  async function resolveQuery() {
    if (!selected) {
      return;
    }

    if (resolution.trim().length < 5) {
      setError("Enter a clear resolution.");
      return;
    }

    try {
      setBusy(true);
      setError("");

      await adminApi.queries.resolve(
        selected.id,
        resolution.trim(),
      );

      setSelected(null);
      setResolution("");

      await loadQueries();
    } catch (err) {
      if (
        err instanceof Error &&
        err.message === "ADMIN_AUTH_REQUIRED"
      ) {
        window.location.href = "/login";
        return;
      }

      setError(
        err instanceof Error
          ? err.message
          : "Unable to resolve query.",
      );
    } finally {
      setBusy(false);
    }
  }

  function openQuery(query: ClientQuery) {
    setSelected(query);
    setResolution(query.resolution || "");
    setError("");
  }

  return (
    <AdminShell>
      <div className="space-y-8">
        {/* Page heading */}
        <div className="flex flex-col justify-between gap-4 lg:flex-row lg:items-end">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">
              Customer Operations
            </p>

            <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-[#111827]">
              Client Queries
            </h1>

            <p className="mt-2 max-w-2xl text-sm leading-6 text-[#6b7280]">
              Review, manage and resolve client support requests
              across the OakPay platform.
            </p>
          </div>

          <button
            type="button"
            onClick={loadQueries}
            disabled={loading}
            className="rounded-xl border border-[#dce3df] bg-white px-4 py-2.5 text-sm font-bold text-[#374151] shadow-sm transition hover:bg-[#f8faf9] disabled:cursor-not-allowed disabled:opacity-50"
          >
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </div>

        {/* Error */}
        {error && (
          <div className="flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50 p-5">
            <ShieldAlert
              size={20}
              className="mt-0.5 shrink-0 text-red-600"
            />

            <div>
              <p className="text-sm font-bold text-red-800">
                Client Queries Error
              </p>

              <p className="mt-1 text-xs leading-5 text-red-700">
                {error}
              </p>
            </div>
          </div>
        )}

        {/* Summary cards */}
        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <MetricCard
            label="Total Queries"
            value={counts.total}
            description="Queries currently loaded"
            icon={<MessageSquare size={20} />}
          />

          <MetricCard
            label="Open"
            value={counts.open}
            description="Awaiting operational attention"
            icon={<Clock3 size={20} />}
          />

          <MetricCard
            label="Escalated"
            value={counts.escalated}
            description="Cases requiring escalation"
            icon={<ShieldAlert size={20} />}
          />

          <MetricCard
            label="Resolved"
            value={counts.resolved}
            description="Successfully resolved queries"
            icon={<CheckCircle2 size={20} />}
          />
        </section>

        {/* Main query workspace */}
        <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          {/* Toolbar */}
          <div className="border-b border-[#e3e8e5] p-5">
            <div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
              {/* Search */}
              <div className="relative w-full xl:max-w-xl">
                <Search
                  size={18}
                  className="absolute left-4 top-1/2 -translate-y-1/2 text-[#9ca3af]"
                />

                <input
                  type="text"
                  value={search}
                  onChange={(event) =>
                    setSearch(event.target.value)
                  }
                  placeholder="Search queries, subjects, categories or IDs..."
                  className="w-full rounded-xl border border-[#dce3df] bg-[#fafcfb] py-3 pl-11 pr-4 text-sm text-[#111827] outline-none transition placeholder:text-[#9ca3af] focus:border-[#397b0a] focus:bg-white focus:ring-2 focus:ring-[#397b0a]/10"
                />
              </div>

              {/* Filter */}
              <div className="flex items-center gap-2">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#edf5ee] text-[#397b0a]">
                  <Filter size={17} />
                </div>

                <select
                  value={status}
                  onChange={(event) =>
                    setStatus(
                      event.target.value as QueryFilter,
                    )
                  }
                  className="rounded-xl border border-[#dce3df] bg-white px-4 py-2.5 text-sm font-semibold text-[#374151] outline-none focus:border-[#397b0a] focus:ring-2 focus:ring-[#397b0a]/10"
                >
                  <option value="ALL">All statuses</option>
                  <option value="OPEN">Open</option>
                  <option value="ASSIGNED">Assigned</option>
                  <option value="ESCALATED">Escalated</option>
                  <option value="RESOLVED">Resolved</option>
                </select>
              </div>
            </div>
          </div>

          {/* Status tabs */}
          <div className="flex gap-2 overflow-x-auto border-b border-[#edf1ef] px-5 py-3">
            <StatusTab
              label="All"
              count={counts.total}
              active={status === "ALL"}
              onClick={() => setStatus("ALL")}
            />

            <StatusTab
              label="Open"
              count={counts.open}
              active={status === "OPEN"}
              onClick={() => setStatus("OPEN")}
            />

            <StatusTab
              label="Assigned"
              count={counts.assigned}
              active={status === "ASSIGNED"}
              onClick={() => setStatus("ASSIGNED")}
            />

            <StatusTab
              label="Escalated"
              count={counts.escalated}
              active={status === "ESCALATED"}
              onClick={() => setStatus("ESCALATED")}
            />

            <StatusTab
              label="Resolved"
              count={counts.resolved}
              active={status === "RESOLVED"}
              onClick={() => setStatus("RESOLVED")}
            />
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-[#e3e8e5] bg-[#fafcfb] text-left">
                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-[#6b7280]">
                    Query
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-[#6b7280]">
                    Category
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-[#6b7280]">
                    Priority
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-[#6b7280]">
                    Status
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-[#6b7280]">
                    Created
                  </th>

                  <th className="px-6 py-4 text-right text-xs font-bold uppercase tracking-wider text-[#6b7280]">
                    Action
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-[#edf1ef]">
                {loading ? (
                  <tr>
                    <td
                      colSpan={6}
                      className="px-6 py-16 text-center"
                    >
                      <div className="flex flex-col items-center">
                        <div className="h-8 w-8 animate-spin rounded-full border-2 border-[#dce8df] border-t-[#397b0a]" />

                        <p className="mt-4 text-sm font-semibold text-[#374151]">
                          Loading client queries...
                        </p>
                      </div>
                    </td>
                  </tr>
                ) : filteredQueries.length === 0 ? (
                  <tr>
                    <td
                      colSpan={6}
                      className="px-6 py-16 text-center"
                    >
                      <div className="mx-auto flex max-w-sm flex-col items-center">
                        <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#edf5ee] text-[#397b0a]">
                          <MessageSquare size={25} />
                        </div>

                        <h3 className="mt-4 text-sm font-extrabold text-[#111827]">
                          No client queries found
                        </h3>

                        <p className="mt-1 text-xs leading-5 text-[#6b7280]">
                          {search
                            ? "Try changing your search or status filter."
                            : "There are currently no queries matching this filter."}
                        </p>
                      </div>
                    </td>
                  </tr>
                ) : (
                  filteredQueries.map((query) => (
                    <tr
                      key={query.id}
                      className="transition hover:bg-[#fafcfb]"
                    >
                      <td className="px-6 py-5">
                        <div className="flex items-start gap-3">
                          <div className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#edf5ee] text-[#397b0a]">
                            <MessageSquare size={17} />
                          </div>

                          <div className="min-w-0">
                            <p className="max-w-sm truncate text-sm font-bold text-[#111827]">
                              {query.subject}
                            </p>

                            <p className="mt-1 text-xs text-[#9ca3af]">
                              {query.id}
                            </p>
                          </div>
                        </div>
                      </td>

                      <td className="px-6 py-5 text-sm text-[#6b7280]">
                        {query.category}
                      </td>

                      <td className="px-6 py-5">
                        <StatusBadge
                          value={query.priority}
                          type="priority"
                        />
                      </td>

                      <td className="px-6 py-5">
                        <StatusBadge
                          value={query.status}
                          type="status"
                        />
                      </td>

                      <td className="whitespace-nowrap px-6 py-5 text-sm text-[#6b7280]">
                        {formatDate(query.createdAt)}
                      </td>

                      <td className="px-6 py-5 text-right">
                        <button
                          type="button"
                          onClick={() => openQuery(query)}
                          className="rounded-lg bg-[#145323] px-3.5 py-2 text-xs font-bold text-white transition hover:bg-[#0b3a1c]"
                        >
                          Open query
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Table footer */}
          <div className="border-t border-[#e3e8e5] bg-[#fafcfb] px-6 py-4">
            <p className="text-xs text-[#6b7280]">
              Showing{" "}
              <span className="font-bold text-[#374151]">
                {filteredQueries.length}
              </span>{" "}
              of{" "}
              <span className="font-bold text-[#374151]">
                {queries.length}
              </span>{" "}
              loaded queries.
            </p>
          </div>
        </section>
      </div>

      {/* Query detail modal */}
      {selected && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white shadow-2xl">
            {/* Modal header */}
            <div className="flex items-start justify-between border-b border-[#e3e8e5] px-6 py-5">
              <div className="flex items-start gap-3">
                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#edf5ee] text-[#397b0a]">
                  <MessageSquare size={20} />
                </div>

                <div>
                  <p className="text-xs font-bold uppercase tracking-[0.12em] text-[#397b0a]">
                    Client Query
                  </p>

                  <h2 className="mt-1 text-xl font-extrabold text-[#111827]">
                    {selected.subject}
                  </h2>

                  <p className="mt-1 text-xs text-[#6b7280]">
                    {selected.id}
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setSelected(null)}
                className="rounded-lg p-2 text-[#6b7280] transition hover:bg-[#f5f7f6] hover:text-[#111827]"
                aria-label="Close query"
              >
                <X size={20} />
              </button>
            </div>

            {/* Modal content */}
            <div className="space-y-6 p-6">
              {/* Query information */}
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <DetailItem
                  label="Status"
                  value={selected.status}
                />

                <DetailItem
                  label="Priority"
                  value={selected.priority}
                />

                <DetailItem
                  label="Category"
                  value={selected.category}
                />

                <DetailItem
                  label="Created"
                  value={formatDate(selected.createdAt)}
                />
              </div>

              {/* Client */}
              <div className="rounded-xl border border-[#e3e8e5] bg-[#fafcfb] p-5">
                <div className="flex items-center gap-3">
                  <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-white text-[#397b0a] shadow-sm">
                    <UserRound size={17} />
                  </div>

                  <div>
                    <p className="text-xs font-bold uppercase tracking-wider text-[#6b7280]">
                      Client
                    </p>

                    <p className="mt-1 text-sm font-bold text-[#111827]">
                      {selected.userId}
                    </p>
                  </div>
                </div>
              </div>

              {/* Description */}
              <div>
                <h3 className="text-sm font-extrabold text-[#111827]">
                  Client message
                </h3>

                <div className="mt-2 rounded-xl border border-[#e3e8e5] bg-white p-4">
                  <p className="whitespace-pre-wrap text-sm leading-6 text-[#4b5563]">
                    {selected.description}
                  </p>
                </div>
              </div>

              {/* Assignment */}
              <div>
                <h3 className="text-sm font-extrabold text-[#111827]">
                  Assignment
                </h3>

                <div className="mt-2 rounded-xl border border-[#e3e8e5] bg-[#fafcfb] p-4">
                  {selected.assignedAdminId ? (
                    <p className="text-sm text-[#4b5563]">
                      Assigned administrator:{" "}
                      <span className="font-bold text-[#111827]">
                        {selected.assignedAdminId}
                      </span>
                    </p>
                  ) : (
                    <p className="text-sm text-[#6b7280]">
                      This query has not yet been assigned to an
                      administrator.
                    </p>
                  )}
                </div>
              </div>

              {/* Resolution */}
              {selected.status !== "RESOLVED" && (
                <div>
                  <h3 className="text-sm font-extrabold text-[#111827]">
                    Resolution
                  </h3>

                  <p className="mt-1 text-xs text-[#6b7280]">
                    Record the action taken to resolve this client
                    query.
                  </p>

                  <textarea
                    value={resolution}
                    onChange={(event) =>
                      setResolution(event.target.value)
                    }
                    rows={5}
                    placeholder="Enter the resolution provided to the client..."
                    className="mt-3 w-full resize-none rounded-xl border border-[#dce3df] bg-white p-4 text-sm leading-6 text-[#111827] outline-none transition placeholder:text-[#9ca3af] focus:border-[#397b0a] focus:ring-2 focus:ring-[#397b0a]/10"
                  />

                  <div className="mt-4 flex justify-end gap-3">
                    <button
                      type="button"
                      onClick={() => setSelected(null)}
                      disabled={busy}
                      className="rounded-xl border border-[#dce3df] bg-white px-4 py-2.5 text-sm font-bold text-[#374151] transition hover:bg-[#f8faf9] disabled:opacity-50"
                    >
                      Cancel
                    </button>

                    <button
                      type="button"
                      onClick={resolveQuery}
                      disabled={busy}
                      className="inline-flex items-center gap-2 rounded-xl bg-[#397b0a] px-5 py-2.5 text-sm font-bold text-white transition hover:bg-[#2f6908] disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      <CheckCircle2 size={17} />

                      {busy
                        ? "Resolving..."
                        : "Resolve query"}
                    </button>
                  </div>
                </div>
              )}

              {/* Existing resolution */}
              {selected.status === "RESOLVED" &&
                selected.resolution && (
                  <div>
                    <h3 className="text-sm font-extrabold text-[#111827]">
                      Resolution
                    </h3>

                    <div className="mt-2 rounded-xl border border-[#dce8df] bg-[#f4f9f4] p-4">
                      <p className="whitespace-pre-wrap text-sm leading-6 text-[#405247]">
                        {selected.resolution}
                      </p>

                      {selected.resolvedAt && (
                        <p className="mt-3 text-xs text-[#6b7280]">
                          Resolved{" "}
                          {formatDate(selected.resolvedAt)}
                        </p>
                      )}
                    </div>
                  </div>
                )}
            </div>
          </div>
        </div>
      )}
    </AdminShell>
  );
}

/* -------------------------------------------------------------------------- */
/* Components                                                                  */
/* -------------------------------------------------------------------------- */

function MetricCard({
  label,
  value,
  description,
  icon,
}: {
  label: string;
  value: number;
  description: string;
  icon: React.ReactNode;
}) {
  return (
    <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between">
        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#edf5ee] text-[#397b0a]">
          {icon}
        </div>

        <span className="text-2xl font-extrabold tracking-tight text-[#111827]">
          {value}
        </span>
      </div>

      <p className="mt-5 text-sm font-bold text-[#374151]">
        {label}
      </p>

      <p className="mt-1 text-xs text-[#6b7280]">
        {description}
      </p>
    </div>
  );
}

function StatusTab({
  label,
  count,
  active,
  onClick,
}: {
  label: string;
  count: number;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={[
        "whitespace-nowrap rounded-lg px-3.5 py-2 text-xs font-bold transition",
        active
          ? "bg-[#397b0a] text-white"
          : "text-[#6b7280] hover:bg-[#f1f5f2] hover:text-[#374151]",
      ].join(" ")}
    >
      {label}

      <span
        className={[
          "ml-1.5 rounded-full px-1.5 py-0.5",
          active
            ? "bg-white/20 text-white"
            : "bg-[#edf1ef] text-[#6b7280]",
        ].join(" ")}
      >
        {count}
      </span>
    </button>
  );
}

function StatusBadge({
  value,
  type,
}: {
  value: string;
  type: "status" | "priority";
}) {
  const normalized = value.toUpperCase();

  let classes =
    "bg-[#f1f5f2] text-[#4b5563]";

  if (type === "status") {
    if (normalized === "OPEN") {
      classes = "bg-blue-50 text-blue-700";
    } else if (normalized === "ASSIGNED") {
      classes = "bg-amber-50 text-amber-700";
    } else if (normalized === "ESCALATED") {
      classes = "bg-red-50 text-red-700";
    } else if (normalized === "RESOLVED") {
      classes = "bg-green-50 text-green-700";
    }
  }

  if (type === "priority") {
    if (normalized === "HIGH" || normalized === "URGENT") {
      classes = "bg-red-50 text-red-700";
    } else if (normalized === "NORMAL") {
      classes = "bg-slate-100 text-slate-700";
    } else if (normalized === "LOW") {
      classes = "bg-green-50 text-green-700";
    }
  }

  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${classes}`}
    >
      {value}
    </span>
  );
}

function DetailItem({
  label,
  value,
}: {
  label: string;
  value: string;
}) {
  return (
    <div className="rounded-xl border border-[#e3e8e5] bg-[#fafcfb] p-4">
      <p className="text-[11px] font-bold uppercase tracking-wider text-[#9ca3af]">
        {label}
      </p>

      <p className="mt-1.5 break-words text-sm font-bold text-[#374151]">
        {value}
      </p>
    </div>
  );
}

function formatDate(value: string | null | undefined) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}