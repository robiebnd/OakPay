"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import type { LucideIcon } from "lucide-react";
import { Activity, BarChart3, CircleDollarSign, FileCheck2, LayoutDashboard, LogOut, MessageSquare, PanelLeftOpen, Scale, Settings, ShieldCheck, Users, WalletCards, X } from "lucide-react";
import { session } from "../lib/api";

type NavItem = { label: string; href: string; icon: LucideIcon };
const mainNavigation: NavItem[] = [
  { label: "Overview", href: "/", icon: LayoutDashboard },
  { label: "KYC Management", href: "/kyc", icon: FileCheck2 },
  { label: "Client Queries", href: "/queries", icon: MessageSquare },
  { label: "Resolution Centre", href: "/resolutions", icon: Scale },
  { label: "Reports", href: "/reports", icon: BarChart3 },
  { label: "Fees & Revenue", href: "/finance", icon: CircleDollarSign },
];
const administrationNavigation: NavItem[] = [
  { label: "Clients", href: "/clients", icon: Users },
  { label: "P2P & Transactions", href: "/transactions", icon: WalletCards },
  { label: "Audit Logs", href: "/audit-logs", icon: Activity },
  { label: "Settings", href: "/settings", icon: Settings },
];

export default function AdminSidebar() {
  const router = useRouter(); const pathname = usePathname();
  const [loggingOut, setLoggingOut] = useState(false); const [mobileOpen, setMobileOpen] = useState(false);
  const isActive = (href: string) => href === "/" ? pathname === "/" : pathname === href || pathname.startsWith(`${href}/`);
  const closeMobileMenu = () => setMobileOpen(false);
  const handleLogout = () => { setLoggingOut(true); session.clear(); router.replace("/login"); };
  const renderNav = (items: NavItem[]) => items.map(item => { const Icon=item.icon; const active=isActive(item.href); return <Link key={item.href} href={item.href} onClick={closeMobileMenu} className={["group flex h-11 items-center gap-3 rounded-xl px-3 text-[13px] font-semibold transition-all",active?"bg-[#397b0a] text-white shadow-sm":"text-white/65 hover:bg-white/7 hover:text-white"].join(" ")}><Icon size={18} strokeWidth={active?2.3:2} className={active?"text-white":"text-white/45 group-hover:text-white/80"}/><span>{item.label}</span>{item.label==="Client Queries"&&<span className={["ml-auto rounded-full px-2 py-0.5 text-[9px] font-bold",active?"bg-white/15 text-white":"bg-white/8 text-white/45"].join(" ")}>LIVE</span>}</Link>; });
  return <><button type="button" aria-label="Open navigation" onClick={()=>setMobileOpen(true)} className="fixed left-4 top-4 z-50 flex h-11 w-11 items-center justify-center rounded-xl border border-white/10 bg-[#082d16] text-white shadow-lg lg:hidden"><PanelLeftOpen size={20}/></button>{mobileOpen&&<button type="button" aria-label="Close navigation" onClick={closeMobileMenu} className="fixed inset-0 z-40 bg-black/40 lg:hidden"/>}<aside className={["fixed inset-y-0 left-0 z-50 flex w-64 flex-col bg-[#082d16] text-white shadow-xl transition-transform duration-200",mobileOpen?"translate-x-0":"-translate-x-full lg:translate-x-0"].join(" ")}><div className="flex h-20 items-center justify-between border-b border-white/10 px-5"><Link href="/" onClick={closeMobileMenu} className="flex items-center gap-3"><div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#397b0a] shadow-sm"><ShieldCheck size={23}/></div><div><div className="text-[17px] font-extrabold tracking-tight">PayOak</div><div className="mt-0.5 text-[10px] font-semibold uppercase tracking-[0.18em] text-white/50">Admin Portal</div></div></Link><button type="button" aria-label="Close navigation" onClick={closeMobileMenu} className="flex h-9 w-9 items-center justify-center rounded-lg text-white/60 lg:hidden"><X size={19}/></button></div><div className="flex-1 overflow-y-auto px-3 py-6"><div className="mb-3 px-3 text-[10px] font-bold uppercase tracking-[0.18em] text-white/35">Operations</div><nav className="space-y-1">{renderNav(mainNavigation)}</nav><div className="mb-3 mt-8 px-3 text-[10px] font-bold uppercase tracking-[0.18em] text-white/35">Administration</div><nav className="space-y-1">{renderNav(administrationNavigation)}</nav></div><div className="border-t border-white/10 p-3"><div className="mb-3 flex items-center gap-3 rounded-xl bg-white/5 px-3 py-3"><ShieldCheck size={16} className="text-[#f5c400]"/><div><p className="text-[11px] font-bold text-white">Secure Session</p><p className="text-[10px] text-white/40">Administrator access</p></div></div><button type="button" onClick={handleLogout} disabled={loggingOut} className="group flex h-11 w-full items-center gap-3 rounded-xl px-3 text-[13px] font-semibold text-white/60 hover:text-red-300 disabled:opacity-50"><LogOut size={18}/><span>{loggingOut?"Signing out...":"Sign out"}</span></button><div className="px-3 pb-1 pt-3 text-[9px] font-medium uppercase tracking-[0.16em] text-white/25">PayOak Admin • Operations</div></div></aside></>;
}
