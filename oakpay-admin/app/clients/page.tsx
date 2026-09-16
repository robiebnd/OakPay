"use client";

import { Search, Users, UserCheck, UserX, ShieldCheck, X, CheckCircle2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import AdminShell from "../../components/AdminShell";
import { adminApi, AdminUser, AdminUserStatus } from "../../lib/adminApi";

export default function ClientsPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [kyc, setKyc] = useState<{ userId: string; status: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<AdminUser | null>(null);
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      setLoading(true); setError("");
      const [allUsers, applications] = await Promise.all([adminApi.users.list(undefined, "CLIENT"), adminApi.kyc.list()]);
      setUsers(allUsers); setKyc(applications.map((item) => ({ userId: item.userId, status: item.status })));
    } catch (e) {
      if (e instanceof Error && e.message === "ADMIN_AUTH_REQUIRED") { window.location.href = "/login"; return; }
      setError(e instanceof Error ? e.message : "Unable to load clients.");
    } finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);
  const verified = useMemo(() => new Set(kyc.filter((x) => x.status === "APPROVED" || x.status === "VERIFIED").map((x) => x.userId)), [kyc]);
  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase(); if (!q) return users;
    return users.filter((u) => `${u.firstName} ${u.lastName} ${u.email} ${u.id}`.toLowerCase().includes(q));
  }, [users, search]);
  const active = users.filter((u) => u.status === "ACTIVE").length;
  const inactive = users.filter((u) => u.status === "INACTIVE").length;

  async function toggleStatus(user: AdminUser) {
    const next: AdminUserStatus = user.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    if (!window.confirm(`${next === "INACTIVE" ? "Deactivate" : "Reactivate"} ${user.email}?`)) return;
    try { setBusy(true); const updated = await adminApi.users.updateStatus(user.id, next); setUsers((cur) => cur.map((x) => x.id === updated.id ? updated : x)); setSelected(updated); }
    catch (e) { setError(e instanceof Error ? e.message : "Unable to update client."); }
    finally { setBusy(false); }
  }
  const date = (v: string) => v ? new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" }).format(new Date(v)) : "—";
  const initials = (u: AdminUser) => `${u.firstName?.[0] ?? ""}${u.lastName?.[0] ?? ""}`.toUpperCase() || u.email[0].toUpperCase();

  return <AdminShell><div className="space-y-8">
    <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between"><div><p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">Administration</p><h1 className="mt-1 text-3xl font-extrabold tracking-tight text-[#111827]">Clients</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-[#6b7280]">Manage PayOak client accounts, account status and KYC information.</p></div><button onClick={load} className="rounded-xl border border-[#dce3df] bg-white px-4 py-2.5 text-sm font-bold text-[#082d16] shadow-sm">Refresh</button></div>
    {error ? <div className="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"><X size={18}/><span className="flex-1">{error}</span></div> : null}
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><Card label="Total Clients" value={loading ? "—" : users.length} icon={<Users size={20}/>}/><Card label="Active" value={loading ? "—" : active} icon={<UserCheck size={20}/>}/><Card label="Inactive" value={loading ? "—" : inactive} icon={<UserX size={20}/>}/><Card label="KYC Verified" value={loading ? "—" : verified.size} icon={<ShieldCheck size={20}/>}/></div>
    <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm"><div className="border-b border-[#e3e8e5] p-5"><div className="relative max-w-xl"><Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-[#9ca3af]"/><input value={search} onChange={(e)=>setSearch(e.target.value)} placeholder="Search clients by name, email or client ID..." className="w-full rounded-xl border border-[#dce3df] bg-[#fafcfb] py-3 pl-11 pr-4 text-sm outline-none focus:border-[#397b0a]"/></div></div>
      {loading ? <div className="flex min-h-[300px] items-center justify-center text-sm text-[#6b7280]">Loading live client data...</div> : filtered.length === 0 ? <div className="flex min-h-[300px] items-center justify-center text-sm text-[#6b7280]">No clients found.</div> : <div className="overflow-x-auto"><table className="w-full min-w-[900px]"><thead><tr className="border-b bg-[#fafcfb] text-left"><th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">Client</th><th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">Status</th><th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">KYC</th><th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">Created</th><th className="px-6 py-4 text-right text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">Action</th></tr></thead><tbody>{filtered.map((u)=><tr key={u.id} className="border-b last:border-0 hover:bg-[#fbfdfc]"><td className="px-6 py-4"><button onClick={()=>setSelected(u)} className="flex items-center gap-3 text-left"><span className="flex h-10 w-10 items-center justify-center rounded-full bg-[#eaf2eb] text-xs font-bold text-[#397b0a]">{initials(u)}</span><span><span className="block text-sm font-semibold text-[#082d16]">{u.firstName} {u.lastName}</span><span className="block text-xs text-[#6b7280]">{u.email}</span><span className="block text-[10px] text-[#9ca3af]">{u.id}</span></span></button></td><td className="px-6 py-4"><span className={`rounded-full px-2.5 py-1 text-xs font-bold ${u.status === "ACTIVE" ? "bg-green-50 text-green-700" : "bg-red-50 text-red-700"}`}>{u.status}</span></td><td className="px-6 py-4">{verified.has(u.id) ? <span className="inline-flex items-center gap-1 text-xs font-bold text-green-700"><CheckCircle2 size={14}/> Verified</span> : <span className="text-xs font-semibold text-[#9a6a00]">Not verified</span>}</td><td className="px-6 py-4 text-xs text-[#6b7280]">{date(u.createdAt)}</td><td className="px-6 py-4 text-right"><button onClick={()=>setSelected(u)} className="text-xs font-bold text-[#397b0a]">Manage →</button></td></tr>)}</tbody></table></div>}
    </section>
    {selected ? <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/40 p-4 sm:items-center"><div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl"><div className="flex items-start justify-between"><div><p className="text-xs font-bold uppercase tracking-wider text-[#397b0a]">Client account</p><h2 className="mt-1 text-xl font-extrabold text-[#111827]">{selected.firstName} {selected.lastName}</h2><p className="mt-1 text-sm text-[#6b7280]">{selected.email}</p></div><button onClick={()=>setSelected(null)}><X size={20}/></button></div><div className="mt-5 space-y-3 rounded-xl bg-[#f7faf8] p-4 text-sm"><p><b>Client ID:</b> {selected.id}</p><p><b>Status:</b> {selected.status}</p><p><b>Email:</b> {selected.emailVerified ? "Verified" : "Pending"}</p><p><b>KYC:</b> {verified.has(selected.id) ? "Verified" : "Not verified"}</p><p><b>Created:</b> {date(selected.createdAt)}</p></div><button disabled={busy} onClick={()=>toggleStatus(selected)} className={`mt-5 w-full rounded-xl px-4 py-3 text-sm font-bold ${selected.status === "ACTIVE" ? "bg-red-50 text-red-700" : "bg-[#082d16] text-white"}`}>{busy ? "Updating..." : selected.status === "ACTIVE" ? "Deactivate account" : "Reactivate account"}</button></div></div> : null}
  </div></AdminShell>;
}
function Card({label,value,icon}:{label:string;value:string|number;icon:React.ReactNode}){return <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm"><div className="flex items-start justify-between"><div><p className="text-xs font-bold uppercase tracking-wider text-[#6b7280]">{label}</p><p className="mt-3 text-3xl font-extrabold text-[#111827]">{value}</p></div><div className="rounded-xl bg-[#edf5ee] p-3 text-[#397b0a]">{icon}</div></div></div>}
