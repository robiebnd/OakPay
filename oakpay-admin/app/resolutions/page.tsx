"use client";

import { useEffect, useState } from "react";
import AdminShell from "../../components/AdminShell";
import { adminApi, ResolutionAudit, ResolutionDispute, session } from "../../lib/api";

function priorityClass(priority: string) {
  if (priority === "Urgent" || priority === "High") return "bg-red-50 text-red-700";
  return "bg-blue-50 text-blue-700";
}
function statusClass(status: string) {
  if (status === "RESOLVED") return "bg-green-50 text-green-700";
  if (status === "DISPUTED" || status === "OPEN") return "bg-red-50 text-red-700";
  return "bg-amber-50 text-amber-700";
}
function formatDate(value: string | null) {
  if (!value) return "—";
  return new Date(value).toLocaleString();
}
function priorityFor(dispute: ResolutionDispute) {
  const ageHours = (Date.now() - new Date(dispute.createdAt).getTime()) / 36e5;
  return ageHours >= 24 ? "Urgent" : "High";
}
function caseNumber(dispute: ResolutionDispute) {
  return `#D-${dispute.id.slice(0, 8).toUpperCase()}`;
}

export default function Resolutions() {
  const [disputes, setDisputes] = useState<ResolutionDispute[]>([]);
  const [selected, setSelected] = useState<ResolutionDispute | null>(null);
  const [audit, setAudit] = useState<ResolutionAudit[]>([]);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [resolving, setResolving] = useState(false);
  const [error, setError] = useState("");
  const [note, setNote] = useState("");
  const [resolution, setResolution] = useState<"BUYER_WINS" | "SELLER_WINS">("BUYER_WINS");

  async function loadDisputes() {
    const token = session.get();
    if (!token) return;
    setLoading(true);
    setError("");
    try { setDisputes(await adminApi.resolutionDisputes(token)); }
    catch (e) { setError(e instanceof Error ? e.message : "Unable to load disputes."); }
    finally { setLoading(false); }
  }

  async function openCase(dispute: ResolutionDispute) {
    const token = session.get();
    if (!token) return;
    setSelected(dispute);
    setAudit([]);
    setNote(dispute.resolutionNote ?? "");
    setResolution("BUYER_WINS");
    setDetailLoading(true);
    setError("");
    try { setAudit(await adminApi.resolutionAudit(token, dispute.id)); }
    catch (e) { setError(e instanceof Error ? e.message : "Unable to load case audit."); }
    finally { setDetailLoading(false); }
  }

  async function resolveCase() {
    if (!selected) return;
    const token = session.get();
    if (!token) return;
    if (!note.trim()) { setError("A resolution note is required before resolving the case."); return; }
    setResolving(true);
    setError("");
    try {
      await adminApi.resolveDispute(token, selected.id, resolution, note.trim());
      setSelected(null);
      await loadDisputes();
    } catch (e) { setError(e instanceof Error ? e.message : "Unable to resolve dispute."); }
    finally { setResolving(false); }
  }

  useEffect(() => { void loadDisputes(); }, []);
  const urgentCount = disputes.filter((d) => priorityFor(d) === "Urgent").length;

  return (
    <AdminShell>
      <div className="space-y-8">
        <div className="flex flex-col justify-between gap-3 md:flex-row md:items-end">
          <div><p className="text-xs font-bold uppercase tracking-[0.12em] text-[#397b0a]">Operations & disputes</p><h1 className="mt-1 text-2xl font-extrabold tracking-tight text-[#111827]">Resolution Centre</h1><p className="mt-2 text-sm text-[#6b7280]">Resolve live P2P payment disputes.</p></div>
          <button type="button" onClick={() => void loadDisputes()} className="w-fit rounded-lg border border-[#d8dfdb] bg-white px-4 py-2 text-xs font-bold text-[#145323] hover:bg-[#f8faf9]">Refresh cases</button>
        </div>

        {error && <div className="rounded-xl border border-red-200 bg-red-50 px-5 py-4 text-sm text-red-800">{error}</div>}

        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm"><p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">Open disputes</p><p className="mt-2 text-3xl font-extrabold text-[#111827]">{disputes.length}</p><p className="mt-1 text-xs text-[#6b7280]">Live from trading service</p></div>
          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm"><p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">Urgent</p><p className="mt-2 text-3xl font-extrabold text-[#111827]">{urgentCount}</p><p className="mt-1 text-xs text-[#6b7280]">Open more than 24 hours</p></div>
          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm"><p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">Pending resolution</p><p className="mt-2 text-3xl font-extrabold text-[#111827]">{disputes.filter((d) => d.status === "OPEN").length}</p><p className="mt-1 text-xs text-[#6b7280]">Awaiting admin decision</p></div>
          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm"><p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">Backend status</p><p className="mt-2 text-lg font-extrabold text-[#145323]">Connected</p><p className="mt-1 text-xs text-[#6b7280]">Admin proxy active</p></div>
        </div>

        <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          <div className="border-b border-[#e3e8e5] px-6 py-5"><h2 className="text-base font-bold text-[#111827]">Active P2P disputes</h2><p className="mt-1 text-xs text-[#6b7280]">Records are retrieved from the trading service through the admin proxy.</p></div>
          <div className="overflow-x-auto"><table className="min-w-full text-left"><thead className="bg-[#f8faf9]"><tr className="border-b border-[#e3e8e5]"><th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">Case</th><th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">Trade</th><th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">Reason</th><th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">Priority</th><th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">Status</th><th className="px-6 py-4 text-right text-xs font-bold uppercase tracking-wide text-[#6b7280]">Action</th></tr></thead><tbody>
            {loading ? <tr><td colSpan={6} className="px-6 py-10 text-center text-sm text-[#6b7280]">Loading live disputes...</td></tr> : disputes.length === 0 ? <tr><td colSpan={6} className="px-6 py-10 text-center text-sm text-[#6b7280]">No open P2P disputes.</td></tr> : disputes.map((dispute) => { const priority = priorityFor(dispute); return <tr key={dispute.id} className="border-b border-[#edf1ef] last:border-b-0 hover:bg-[#fafcfb]"><td className="whitespace-nowrap px-6 py-4 text-sm font-bold text-[#111827]">{caseNumber(dispute)}</td><td className="whitespace-nowrap px-6 py-4 font-mono text-xs text-[#6b7280]">{dispute.tradeId}</td><td className="max-w-md px-6 py-4 text-sm text-[#374151]">{dispute.reason}</td><td className="px-6 py-4"><span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${priorityClass(priority)}`}>{priority}</span></td><td className="px-6 py-4"><span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${statusClass(dispute.status)}`}>{dispute.status}</span></td><td className="px-6 py-4 text-right"><button type="button" onClick={() => void openCase(dispute)} className="rounded-lg bg-[#145323] px-3 py-2 text-xs font-bold text-white transition hover:bg-[#0b3a1c]">Open case</button></td></tr>; })}
          </tbody></table></div>
        </section>

        {selected && <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" role="dialog" aria-modal="true"><div className="max-h-[90vh] w-full max-w-3xl overflow-y-auto rounded-2xl bg-white shadow-2xl">
          <div className="flex items-start justify-between border-b border-[#e3e8e5] px-6 py-5"><div><p className="text-xs font-bold uppercase tracking-wide text-[#397b0a]">P2P dispute</p><h2 className="mt-1 text-xl font-extrabold text-[#111827]">{caseNumber(selected)}</h2><p className="mt-1 font-mono text-xs text-[#6b7280]">Trade {selected.tradeId}</p></div><button type="button" onClick={() => setSelected(null)} className="rounded-lg px-3 py-2 text-sm font-bold text-[#6b7280] hover:bg-[#f3f5f4]">Close</button></div>
          <div className="space-y-6 p-6">
            <div className="grid gap-4 sm:grid-cols-2"><div className="rounded-xl bg-[#f8faf9] p-4"><p className="text-xs font-bold uppercase tracking-wide text-[#6b7280]">Opened by</p><p className="mt-2 break-all text-sm text-[#111827]">{selected.openedBy}</p></div><div className="rounded-xl bg-[#f8faf9] p-4"><p className="text-xs font-bold uppercase tracking-wide text-[#6b7280]">Created</p><p className="mt-2 text-sm text-[#111827]">{formatDate(selected.createdAt)}</p></div></div>
            <div><h3 className="text-sm font-bold text-[#111827]">Dispute reason</h3><p className="mt-2 rounded-xl border border-[#e3e8e5] bg-white p-4 text-sm leading-6 text-[#374151]">{selected.reason}</p></div>
            {selected.evidence && <div><h3 className="text-sm font-bold text-[#111827]">Evidence</h3><p className="mt-2 break-words rounded-xl border border-[#e3e8e5] bg-white p-4 text-sm leading-6 text-[#374151]">{selected.evidence}</p></div>}
            <div><h3 className="text-sm font-bold text-[#111827]">Audit trail</h3>{detailLoading ? <p className="mt-3 text-sm text-[#6b7280]">Loading audit...</p> : audit.length === 0 ? <p className="mt-3 text-sm text-[#6b7280]">No audit events returned.</p> : <div className="mt-3 space-y-2">{audit.map((event) => <div key={event.id} className="rounded-xl border border-[#e3e8e5] p-4"><div className="flex flex-col justify-between gap-1 sm:flex-row"><span className="text-xs font-bold text-[#145323]">{event.eventType}</span><span className="text-xs text-[#6b7280]">{formatDate(event.createdAt)}</span></div>{event.note && <p className="mt-2 text-sm text-[#374151]">{event.note}</p>}</div>)}</div>}</div>
            <div className="border-t border-[#e3e8e5] pt-6"><h3 className="text-sm font-bold text-[#111827]">Resolve dispute</h3><p className="mt-1 text-xs text-[#6b7280]">This action executes the corresponding escrow outcome and writes a resolution audit event.</p><div className="mt-4 grid gap-3 sm:grid-cols-2"><button type="button" onClick={() => setResolution("BUYER_WINS")} className={`rounded-xl border px-4 py-3 text-left ${resolution === "BUYER_WINS" ? "border-[#145323] bg-[#f2f8f3]" : "border-[#e3e8e5]"}`}><span className="block text-sm font-bold text-[#111827]">Buyer wins</span><span className="mt-1 block text-xs text-[#6b7280]">Release escrow to the buyer.</span></button><button type="button" onClick={() => setResolution("SELLER_WINS")} className={`rounded-xl border px-4 py-3 text-left ${resolution === "SELLER_WINS" ? "border-[#145323] bg-[#f2f8f3]" : "border-[#e3e8e5]"}`}><span className="block text-sm font-bold text-[#111827]">Seller wins</span><span className="mt-1 block text-xs text-[#6b7280]">Unlock the seller's escrowed asset.</span></button></div><textarea value={note} onChange={(e) => setNote(e.target.value)} rows={4} placeholder="Enter the reason for the decision..." className="mt-4 w-full rounded-xl border border-[#d8dfdb] p-4 text-sm outline-none focus:border-[#145323]" /><button type="button" disabled={resolving || detailLoading} onClick={() => void resolveCase()} className="mt-4 w-full rounded-xl bg-[#145323] px-4 py-3 text-sm font-bold text-white hover:bg-[#0b3a1c] disabled:cursor-not-allowed disabled:opacity-60">{resolving ? "Resolving dispute..." : `Resolve as ${resolution === "BUYER_WINS" ? "Buyer Wins" : "Seller Wins"}`}</button></div>
          </div>
        </div></div>}
      </div>
    </AdminShell>
  );
}