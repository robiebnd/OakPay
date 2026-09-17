"use client";

import { Activity, Clock3, Search, ShieldCheck, RefreshCw } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import AdminShell from "../../components/AdminShell";
import { adminApi, P2PDisputeAudit } from "../../lib/adminApi";

type EventRow = P2PDisputeAudit & { label: string };

export default function AuditLogsPage() {
  const [events, setEvents] = useState<EventRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");

  async function load(initial = false) {
    try {
      if (initial) setLoading(true);
      else setRefreshing(true);
      setError("");

      const disputes = await adminApi.disputes.list();
      const audits = await Promise.all(disputes.map((d) => adminApi.disputes.audit(d.id)));
      const merged = audits
        .flat()
        .map((e) => ({ ...e, label: e.eventType.replaceAll("_", " ") }))
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

      setEvents(merged);
    } catch (e) {
      if (e instanceof Error && e.message === "ADMIN_AUTH_REQUIRED") {
        setError("Your administrator session has expired. Please sign in again.");
        return;
      }
      setError(e instanceof Error ? e.message : "Unable to load audit events.");
    } finally {
      if (initial) setLoading(false);
      setRefreshing(false);
    }
  }

  useEffect(() => {
    load(true);
    const t = window.setInterval(() => load(false), 15000);
    return () => window.clearInterval(t);
  }, []);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return events;
    return events.filter((e) =>
      [e.eventType, e.tradeId, e.actorId, e.disputeId, e.note || ""].some((v) => v.toLowerCase().includes(q)),
    );
  }, [events, search]);

  const today = new Date();
  const dayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
  const todayCount = events.filter((e) => new Date(e.createdAt).getTime() >= dayStart).length;
  const adminActions = events.filter((e) => e.eventType.includes("RESOLVED")).length;
  const latest = events[0]
    ? new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" }).format(new Date(events[0].createdAt))
    : "—";
  const dt = (v: string) =>
    new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" }).format(new Date(v));

  return (
    <AdminShell>
      <div className="space-y-8">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">Administration</p>
            <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-[#111827]">Audit Logs</h1>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-[#6b7280]">Live P2P operational audit activity from the PayOak Resolution Centre.</p>
          </div>
          <button
            onClick={() => load(false)}
            disabled={refreshing}
            className="inline-flex items-center gap-2 rounded-xl border border-[#dce3df] bg-white px-4 py-2.5 text-sm font-bold text-[#082d16] shadow-sm disabled:opacity-60"
          >
            <RefreshCw size={15} className={refreshing ? "animate-spin" : ""} />
            Refresh
          </button>
        </div>

        {error ? <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</div> : null}

        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <Card label="Events Today" value={loading ? "—" : todayCount} icon={<Activity size={20} />} />
          <Card label="Admin Actions" value={loading ? "—" : adminActions} icon={<ShieldCheck size={20} />} />
          <Card label="Latest Activity" value={loading ? "—" : latest} icon={<Clock3 size={20} />} />
        </div>

        <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          <div className="border-b border-[#e3e8e5] p-5">
            <div className="relative max-w-xl">
              <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-[#9ca3af]" />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search audit events..."
                className="w-full rounded-xl border border-[#dce3df] bg-[#fafcfb] py-3 pl-11 pr-4 text-sm outline-none focus:border-[#397b0a]"
              />
            </div>
          </div>

          <div className="border-b border-[#edf1ef] bg-[#fafcfb] px-6 py-4">
            <div className="grid grid-cols-4 gap-4 text-xs font-bold uppercase tracking-wider text-[#6b7280]">
              <span>Event</span><span>Actor</span><span>Timestamp</span><span>Reference</span>
            </div>
          </div>

          {loading ? (
            <div className="flex min-h-[250px] items-center justify-center text-sm text-[#6b7280]">Loading live audit data...</div>
          ) : filtered.length === 0 ? (
            <div className="flex min-h-[250px] items-center justify-center text-sm text-[#6b7280]">No audit events found.</div>
          ) : (
            <div className="divide-y divide-[#edf1ef]">
              {filtered.map((e) => (
                <div key={e.id} className="grid grid-cols-4 gap-4 px-6 py-5 hover:bg-[#fbfdfc]">
                  <div>
                    <p className="text-sm font-bold capitalize text-[#111827]">{e.label.toLowerCase()}</p>
                    <p className="mt-1 text-xs text-[#6b7280]">{e.note || "P2P operational event"}</p>
                  </div>
                  <div>
                    <p className="text-xs font-semibold text-[#374151]">{e.actorId.slice(0, 12)}…</p>
                    <p className="mt-1 text-[10px] text-[#9ca3af]">Dispute actor</p>
                  </div>
                  <div className="text-xs text-[#6b7280]">{dt(e.createdAt)}</div>
                  <div>
                    <p className="text-xs font-semibold text-[#397b0a]">Trade {e.tradeId.slice(0, 8)}…</p>
                    <p className="mt-1 text-[10px] text-[#9ca3af]">Dispute {e.disputeId.slice(0, 8)}…</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </AdminShell>
  );
}

function Card({ label, value, icon }: { label: string; value: string | number; icon: React.ReactNode }) {
  return (
    <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-bold uppercase tracking-wider text-[#6b7280]">{label}</p>
          <p className="mt-3 text-3xl font-extrabold text-[#111827]">{value}</p>
        </div>
        <div className="rounded-xl bg-[#edf5ee] p-3 text-[#397b0a]">{icon}</div>
      </div>
    </div>
  );
}
