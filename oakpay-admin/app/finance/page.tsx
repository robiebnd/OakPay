"use client";

import { Check, CheckCircle2, CircleDollarSign, Clock3, FileText, Percent, RefreshCw, Save, Search } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import AdminShell from "../../components/AdminShell";
import { AdminCommissionRecord, AdminFinancialSummary, adminApi } from "../../lib/adminApi";

const feeDefinitions = [
  { key: "TRADING_FEE_RATE", label: "Trading fee", description: "Spot trading fee applied to each side of a matched trade.", type: "rate" },
  { key: "P2P_COMMISSION_RATE", label: "P2P commission", description: "P2P commission assessed on the configured trade amount.", type: "rate" },
  { key: "P2P_UNVERIFIED_USD_LIMIT", label: "Unverified P2P limit", description: "Maximum USD-equivalent value an unverified account may trade per transaction.", type: "amount" },
] as const;

type FeeKey = typeof feeDefinitions[number]["key"];

export default function FinancePage() {
  const [summary, setSummary] = useState<AdminFinancialSummary | null>(null);
  const [commissions, setCommissions] = useState<AdminCommissionRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [search, setSearch] = useState("");
  const [drafts, setDrafts] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState<FeeKey | "">("");
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [collectionReference, setCollectionReference] = useState("");
  const [collectionMethod, setCollectionMethod] = useState("");
  const [collecting, setCollecting] = useState(false);

  const load = useCallback(async (initial = false) => {
    try {
      if (initial) setLoading(true);
      else setRefreshing(true);
      setError("");
      const financial = await adminApi.finance.summary();
      const rows = await adminApi.finance.commissions(statusFilter || undefined);
      setSummary(financial);
      setCommissions(rows);
      setDrafts(current => {
        const next = { ...current };
        feeDefinitions.forEach(fee => {
          if (next[fee.key] == null && financial.currentFees[fee.key] != null) {
            next[fee.key] = fee.type === "rate" ? String(Number(financial.currentFees[fee.key]) * 100) : String(financial.currentFees[fee.key]);
          }
        });
        return next;
      });
    } catch (e) {
      if (e instanceof Error && e.message === "ADMIN_AUTH_REQUIRED") {
        window.location.href = "/login";
        return;
      }
      setError(e instanceof Error ? e.message : "Unable to load financial data.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [statusFilter]);

  useEffect(() => { void load(true); }, [load]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return commissions;
    return commissions.filter(row =>
      [row.tradeId, row.payerId, row.fiatCurrency, row.collectionReference || "", row.collectionMethod || "", row.status]
        .some(value => String(value).toLowerCase().includes(q))
    );
  }, [commissions, search]);

  const selectableRows = useMemo(() => filtered.filter(row => row.status === "ASSESSED"), [filtered]);
  const selectedRows = useMemo(() => selectableRows.filter(row => selectedIds.has(row.id)), [selectableRows, selectedIds]);
  const allVisibleSelected = selectableRows.length > 0 && selectableRows.every(row => selectedIds.has(row.id));

  function displayFee(key: FeeKey, value: number | undefined) {
    if (value == null) return "—";
    if (key.endsWith("_RATE")) return (Number(value) * 100).toFixed(2) + "%";
    return Number(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + " USD";
  }

  async function saveFee(key: FeeKey) {
    const raw = drafts[key];
    const value = Number(raw);
    if (!Number.isFinite(value) || value < 0) {
      setError("Enter a valid non-negative value.");
      return;
    }
    const backendValue = key.endsWith("_RATE") ? value / 100 : value;
    try {
      setSaving(key);
      setError("");
      await adminApi.finance.updateFee(key, backendValue);
      await load(false);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to update fee setting.");
    } finally {
      setSaving("");
    }
  }

  async function collect() {
    if (!selectedRows.length) return;
    if (!collectionReference.trim() || !collectionMethod.trim()) {
      setError("Collection reference and collection method are required.");
      return;
    }
    try {
      setCollecting(true);
      setError("");
      for (const row of selectedRows) {
        await adminApi.finance.collectCommission(row.tradeId, collectionReference.trim(), collectionMethod.trim());
      }
      setSelectedIds(new Set());
      setCollectionReference("");
      setCollectionMethod("");
      await load(false);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to record commission collection.");
    } finally {
      setCollecting(false);
    }
  }

  return (
    <AdminShell>
      <div className="space-y-7">
        <header className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#397b0a]">Administration</p>
            <h1 className="mt-1 text-4xl font-extrabold tracking-tight text-[#0b1628]">Fees &amp; Revenue</h1>
            <p className="mt-2 max-w-3xl text-sm leading-6 text-[#667085]">Control current fee rates and monitor P2P commission collection activity.</p>
          </div>
          <button type="button" onClick={() => void load(false)} disabled={refreshing} className="inline-flex w-fit items-center gap-2 rounded-xl border border-[#dce3df] bg-white px-4 py-3 text-sm font-bold text-[#082d16] shadow-sm disabled:opacity-60">
            <RefreshCw size={16} className={refreshing ? "animate-spin" : ""} /> Refresh
          </button>
        </header>

        {error ? <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</div> : null}

        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {feeDefinitions.map(fee => (
            <div key={fee.key} className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#edf5e9] text-[#397b0a]">
                  {fee.type === "rate" ? <Percent size={18} /> : <CircleDollarSign size={18} />}
                </div>
                <span className="rounded-full bg-[#f6faf4] px-3 py-1 text-[11px] font-bold text-[#397b0a]">
                  {displayFee(fee.key, summary?.currentFees[fee.key])}
                </span>
              </div>
              <h2 className="mt-5 text-base font-extrabold text-[#111827]">{fee.label}</h2>
              <p className="mt-2 min-h-[48px] text-xs leading-5 text-[#667085]">{fee.description}</p>
              <div className="mt-4 flex gap-2">
                <input value={drafts[fee.key] || ""} onChange={e => setDrafts(current => ({ ...current, [fee.key]: e.target.value }))} inputMode="decimal" className="min-w-0 flex-1 rounded-xl border border-[#dce3df] bg-[#fafcfb] px-3 py-2.5 text-sm font-semibold outline-none focus:border-[#397b0a]" />
                <button type="button" onClick={() => void saveFee(fee.key)} disabled={saving === fee.key || loading} className="inline-flex items-center gap-2 rounded-xl bg-[#397b0a] px-4 py-2.5 text-sm font-bold text-white disabled:opacity-50">
                  <Save size={15} /> {saving === fee.key ? "Saving..." : "Save"}
                </button>
              </div>
            </div>
          ))}
        </section>

        <section className="grid gap-5 xl:grid-cols-2">
          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-6 shadow-sm">
            <h2 className="text-base font-extrabold text-[#111827]">Trading fee activity</h2>
            <p className="mt-1 text-xs text-[#667085]">Fees charged on executed spot trades, grouped by quote currency.</p>
            <div className="mt-5 overflow-x-auto">
              {summary?.spotFees.length ? (
                <table className="w-full min-w-[620px] text-left text-sm">
                  <thead><tr className="border-b text-xs text-[#667085]"><th className="pb-3">Currency</th><th className="pb-3">Trades</th><th className="pb-3">Volume</th><th className="pb-3">Buyer fees</th><th className="pb-3">Seller fees</th><th className="pb-3">Total</th></tr></thead>
                  <tbody>{summary.spotFees.map(row => <tr key={row.quoteCurrency} className="border-b last:border-0"><td className="py-3 font-bold">{row.quoteCurrency}</td><td className="py-3">{row.tradeCount}</td><td className="py-3">{Number(row.grossVolume).toLocaleString()}</td><td className="py-3">{Number(row.buyerFees).toLocaleString(undefined,{minimumFractionDigits:2})}</td><td className="py-3">{Number(row.sellerFees).toLocaleString(undefined,{minimumFractionDigits:2})}</td><td className="py-3 font-extrabold">{Number(row.buyerFees + row.sellerFees).toLocaleString(undefined,{minimumFractionDigits:2})}</td></tr>)}</tbody>
                </table>
              ) : <p className="py-6 text-sm text-[#667085]">No executed trading fee activity yet.</p>}
            </div>
          </div>

          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-6 shadow-sm">
            <h2 className="text-base font-extrabold text-[#111827]">P2P commission summary</h2>
            <p className="mt-1 text-xs text-[#667085]">Amounts are kept separate by fiat currency.</p>
            <div className="mt-5 space-y-3">
              {summary?.p2pCommissions.length ? summary.p2pCommissions.map(row => (
                <div key={row.fiatCurrency} className="rounded-xl border border-[#edf1ef] p-4">
                  <div className="flex items-center justify-between"><span className="text-sm font-extrabold">{row.fiatCurrency}</span><span className="text-xs text-[#667085]">{row.commissionCount} records</span></div>
                  <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-4"><Metric label="Assessed" value={row.assessed}/><Metric label="Collected" value={row.collected}/><Metric label="Outstanding" value={row.outstanding}/><Metric label="Waived" value={row.waived}/></div>
                </div>
              )) : <p className="py-6 text-sm text-[#667085]">No P2P commissions have been assessed yet.</p>}
            </div>
          </div>
        </section>

        <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          <div className="flex flex-col gap-4 border-b border-[#e3e8e5] p-5 lg:flex-row lg:items-center lg:justify-between">
            <div><h2 className="text-xl font-extrabold text-[#111827]">Record Collections</h2><p className="mt-1 text-sm text-[#667085]">Select the commission records you want to collect and process.</p></div>
            <div className="flex flex-col gap-2 sm:flex-row">
              <select className="rounded-xl border border-[#dce3df] bg-white px-3 py-2.5 text-sm font-semibold outline-none"><option>All categories</option><option>P2P Commissions</option></select>
              <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)} className="rounded-xl border border-[#dce3df] bg-white px-3 py-2.5 text-sm font-semibold outline-none"><option value="">All statuses</option><option value="ASSESSED">Assessed</option><option value="COLLECTED">Collected</option><option value="WAIVED">Waived</option></select>
              <div className="relative"><Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#98a2b3]" /><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search items..." className="rounded-xl border border-[#dce3df] bg-[#fafcfb] py-2.5 pl-9 pr-3 text-sm outline-none focus:border-[#397b0a]" /></div>
              <button type="button" onClick={() => { setSearch(""); setStatusFilter(""); setSelectedIds(new Set()); }} className="rounded-xl bg-[#f7f9f8] px-4 py-2.5 text-sm font-semibold text-[#344054]">Reset</button>
            </div>
          </div>
          <div className="overflow-x-auto">
            {filtered.length ? (
              <table className="w-full min-w-[1120px] text-left text-sm">
                <thead><tr className="border-b bg-[#fafcfb] text-xs text-[#667085]"><th className="w-14 px-5 py-3"><input aria-label="Select all visible commission records" type="checkbox" checked={allVisibleSelected} onChange={e => { const next = new Set(selectedIds); if (e.target.checked) selectableRows.forEach(row => next.add(row.id)); else selectableRows.forEach(row => next.delete(row.id)); setSelectedIds(next); }} className="h-5 w-5 accent-[#397b0a]" /></th><th className="px-5 py-3">#</th><th className="px-5 py-3">Item</th><th className="px-5 py-3">Payer</th><th className="px-5 py-3">Fiat</th><th className="px-5 py-3">Commission</th><th className="px-5 py-3">Status</th><th className="px-5 py-3">Collection</th><th className="px-5 py-3"></th></tr></thead>
                <tbody>{filtered.map((row, index) => <tr key={row.id} className="border-b last:border-0 hover:bg-[#fbfdfc]">
                  <td className="px-5 py-4"><input aria-label={"Select commission " + row.tradeId} type="checkbox" disabled={row.status !== "ASSESSED"} checked={selectedIds.has(row.id)} onChange={e => { const next = new Set(selectedIds); if (e.target.checked) next.add(row.id); else next.delete(row.id); setSelectedIds(next); }} className="h-5 w-5 accent-[#397b0a] disabled:opacity-30" /></td>
                  <td className="px-5 py-4 text-xs text-[#667085]">{index + 1}</td><td className="px-5 py-4"><p className="font-bold">{row.tradeId.slice(0,8)}…</p><p className="mt-1 text-[11px] text-[#98a2b3]">{new Date(row.createdAt).toLocaleString()}</p></td><td className="px-5 py-4 text-xs">{row.payerId.slice(0,12)}…</td><td className="px-5 py-4">{row.fiatCurrency}</td><td className="px-5 py-4 font-extrabold">{Number(row.commissionAmount).toLocaleString(undefined,{minimumFractionDigits:2})}</td>
                  <td className="px-5 py-4"><span className={row.status==="COLLECTED" ? "inline-flex items-center gap-1.5 rounded-full bg-green-50 px-2.5 py-1 text-xs font-bold text-green-700" : row.status==="WAIVED" ? "inline-flex items-center gap-1.5 rounded-full bg-slate-100 px-2.5 py-1 text-xs font-bold text-slate-600" : "inline-flex items-center gap-1.5 rounded-full bg-amber-50 px-2.5 py-1 text-xs font-bold text-amber-700"}>{row.status==="COLLECTED" ? <CheckCircle2 size={13}/> : row.status==="ASSESSED" ? <Clock3 size={13}/> : null}{row.status}</span></td>
                  <td className="px-5 py-4">{row.collectionReference ? <><p className="text-xs font-semibold">{row.collectionReference}</p><p className="text-[11px] text-[#667085]">{row.collectionMethod || "—"}</p></> : <span className="text-xs text-[#98a2b3]">Not recorded</span>}</td><td className="px-5 py-4 text-right"><FileText size={19} className="ml-auto text-[#718096]" /></td>
                </tr>)}</tbody>
              </table>
            ) : <div className="min-h-[220px] flex items-center justify-center text-sm text-[#667085]">No matching commission records.</div>}
          </div>
          <div className="flex flex-col gap-4 border-t border-[#e3e8e5] bg-white p-5 sm:flex-row sm:items-center sm:justify-between">
            <label className="flex items-center gap-3 text-sm font-semibold text-[#344054]"><input type="checkbox" checked={allVisibleSelected} onChange={e => { const next = new Set(selectedIds); if (e.target.checked) selectableRows.forEach(row => next.add(row.id)); else selectableRows.forEach(row => next.delete(row.id)); setSelectedIds(next); }} className="h-5 w-5 accent-[#397b0a]" />Select all ({selectableRows.length} items)</label>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center"><div className="text-sm"><p className="font-extrabold text-[#111827]">{selectedRows.length} item{selectedRows.length === 1 ? "" : "s"} selected</p><p className="text-xs text-[#667085]">This will record collections for the selected items.</p></div><button type="button" disabled={!selectedRows.length || collecting} onClick={() => { setCollectionReference(""); setCollectionMethod(""); }} className="inline-flex items-center justify-center gap-2 rounded-xl bg-[#397b0a] px-5 py-3 text-sm font-bold text-white shadow-sm disabled:opacity-40"><CircleDollarSign size={17}/>Record collection</button></div>
          </div>
        </section>        {selectedRows.length ? <div className="fixed inset-0 z-[80] flex items-center justify-center bg-black/40 p-4"><div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl">
          <h2 className="text-lg font-extrabold text-[#111827]">Record commission collection</h2>
          <p className="mt-2 text-sm text-[#667085]">{selectedRows.length} commission record{selectedRows.length === 1 ? "" : "s"} selected · {selectedRows.reduce((sum,row) => sum + Number(row.commissionAmount), 0).toLocaleString(undefined,{minimumFractionDigits:2})} total</p>
          <div className="mt-5 space-y-4"><label className="block"><span className="mb-1.5 block text-xs font-bold text-[#374151]">Collection reference</span><input value={collectionReference} onChange={e => setCollectionReference(e.target.value)} placeholder="e.g. BANK-REF-12345" className="w-full rounded-xl border border-[#dce3df] px-3 py-3 text-sm outline-none focus:border-[#397b0a]" /></label><label className="block"><span className="mb-1.5 block text-xs font-bold text-[#374151]">Collection method</span><input value={collectionMethod} onChange={e => setCollectionMethod(e.target.value)} placeholder="BANK_TRANSFER" className="w-full rounded-xl border border-[#dce3df] px-3 py-3 text-sm outline-none focus:border-[#397b0a]" /></label></div>
          <div className="mt-6 flex justify-end gap-3"><button type="button" onClick={() => setSelectedIds(new Set())} disabled={collecting} className="rounded-xl border border-[#dce3df] px-4 py-2.5 text-sm font-bold">Cancel</button><button type="button" onClick={() => void collect()} disabled={collecting} className="rounded-xl bg-[#397b0a] px-4 py-2.5 text-sm font-bold text-white">{collecting ? "Recording..." : "Record collection"}</button></div>
          <p className="mt-4 text-[11px] leading-5 text-[#667085]">This records the external collection reference and method for the selected commissions. It does not move funds between wallets.</p>
        </div></div> : null}      </div>
    </AdminShell>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return <div><p className="text-[11px] text-[#98a2b3]">{label}</p><p className="mt-1 text-sm font-extrabold text-[#344054]">{Number(value).toLocaleString(undefined,{minimumFractionDigits:2})}</p></div>;
}
