"use client";

import { ArrowDownLeft, ArrowUpRight, BarChart3, Search, Wallet, RefreshCw } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import AdminShell from "../../components/AdminShell";
import { AdminTransaction, adminApi } from "../../lib/adminApi";

export default function TransactionsPage() {
  const [items, setItems] = useState<AdminTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");

  async function load(initial = false) {
    try {
      if (initial) setLoading(true);
      else setRefreshing(true);
      setError("");
      const result = await adminApi.transactions.list(200);
      setItems(result);
    } catch (e) {
      if (e instanceof Error && e.message === "ADMIN_AUTH_REQUIRED") {
        setError("Your administrator session has expired. Please sign in again.");
        return;
      }
      setError(e instanceof Error ? e.message : "Unable to load transactions.");
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
    if (!q) return items;
    return items.filter(x =>
      [x.id, x.buyerId, x.sellerId, x.asset, x.fiatCurrency, x.paymentMethod, x.status, x.paymentReference || ""]
        .some(v => String(v).toLowerCase().includes(q))
    );
  }, [items, search]);

  const incoming = items.filter(x => x.status !== "CANCELLED" && x.status !== "EXPIRED").length;
  const outgoing = incoming;
  const total = items.length;
  const fmt = (n: number | string) => Number(n || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 8 });
  const dt = (v: string) => new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" }).format(new Date(v));

  return (
    <AdminShell>
      <div className="space-y-8">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">Administration</p>
            <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-[#111827]">P2P & Transactions</h1>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-[#6b7280]">Monitor live PayOak P2P trades and transaction activity.</p>
          </div>
          <button onClick={() => load(false)} disabled={refreshing} className="inline-flex items-center gap-2 rounded-xl border border-[#dce3df] bg-white px-4 py-2.5 text-sm font-bold text-[#082d16] shadow-sm disabled:opacity-60">
            <RefreshCw size={15} className={refreshing ? "animate-spin" : ""} />Refresh
          </button>
        </div>

        {error ? <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</div> : null}

        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <Card label="Transactions" value={loading ? "—" : total} icon={<BarChart3 size={20} />} />
          <Card label="P2P Trades" value={loading ? "—" : total} icon={<Wallet size={20} />} />
          <Card label="Incoming" value={loading ? "—" : incoming} icon={<ArrowDownLeft size={20} />} />
          <Card label="Outgoing" value={loading ? "—" : outgoing} icon={<ArrowUpRight size={20} />} />
        </div>

        <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          <div className="flex items-center justify-between gap-4 border-b border-[#e3e8e5] p-5">
            <div className="relative max-w-xl flex-1">
              <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-[#9ca3af]" />
              <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search transaction ID, wallet or user..." className="w-full rounded-xl border border-[#dce3df] bg-[#fafcfb] py-3 pl-11 pr-4 text-sm outline-none focus:border-[#397b0a]" />
            </div>
            {!loading && refreshing ? <span className="shrink-0 text-xs font-semibold text-[#6b7280]">Updating…</span> : null}
          </div>

          {loading ? (
            <div className="flex min-h-[300px] items-center justify-center text-sm text-[#6b7280]">Loading live transaction data...</div>
          ) : filtered.length === 0 ? (
            <div className="flex min-h-[300px] items-center justify-center text-sm text-[#6b7280]">No transactions found.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1100px]">
                <thead><tr className="border-b bg-[#fafcfb] text-left"><th className="px-5 py-4 text-[11px] font-bold uppercase text-[#6b7280]">Transaction</th><th className="px-5 py-4 text-[11px] font-bold uppercase text-[#6b7280]">Asset</th><th className="px-5 py-4 text-[11px] font-bold uppercase text-[#6b7280]">Amount</th><th className="px-5 py-4 text-[11px] font-bold uppercase text-[#6b7280]">Fiat total</th><th className="px-5 py-4 text-[11px] font-bold uppercase text-[#6b7280]">Status</th><th className="px-5 py-4 text-[11px] font-bold uppercase text-[#6b7280]">Created</th></tr></thead>
                <tbody>{filtered.map(x => <tr key={x.id} className="border-b last:border-0 hover:bg-[#fbfdfc]"><td className="px-5 py-4"><p className="text-sm font-semibold text-[#082d16]">{x.id.slice(0, 8)}…</p><p className="mt-1 text-[10px] text-[#9ca3af]">Buyer {x.buyerId.slice(0, 8)} · Seller {x.sellerId.slice(0, 8)}</p></td><td className="px-5 py-4"><p className="text-sm font-bold text-[#111827]">{x.asset}</p><p className="text-xs text-[#6b7280]">{x.paymentMethod}</p></td><td className="px-5 py-4 text-sm font-bold">{fmt(x.quantity)} {x.asset}</td><td className="px-5 py-4 text-sm font-bold">{fmt(x.fiatAmount)} {x.fiatCurrency}</td><td className="px-5 py-4"><span className={`rounded-full px-2.5 py-1 text-xs font-bold ${x.status === "COMPLETED" ? "bg-green-50 text-green-700" : x.status === "CANCELLED" || x.status === "EXPIRED" ? "bg-red-50 text-red-700" : "bg-amber-50 text-amber-700"}`}>{x.status}</span></td><td className="px-5 py-4 text-xs text-[#6b7280]">{dt(x.createdAt)}</td></tr>)}</tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </AdminShell>
  );
}

function Card({ label, value, icon }: { label: string; value: string | number; icon: React.ReactNode }) {
  return <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm"><div className="flex items-start justify-between"><div><p className="text-xs font-bold uppercase tracking-wider text-[#6b7280]">{label}</p><p className="mt-3 text-3xl font-extrabold text-[#111827]">{value}</p></div><div className="rounded-xl bg-[#edf5ee] p-3 text-[#397b0a]">{icon}</div></div></div>;
}
