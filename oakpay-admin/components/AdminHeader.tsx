"use client";

import { Bell, Search, ShieldCheck } from "lucide-react";

export default function AdminHeader() {
  return (
    <header className="sticky top-0 z-30 flex h-20 items-center justify-between border-b border-[#e3e8e5] bg-white/95 px-5 backdrop-blur md:px-8">
      <div>
        <p className="text-xs font-bold uppercase tracking-[0.12em] text-[#397b0a]">
          PayOak Operations
        </p>

        <h1 className="mt-0.5 text-lg font-extrabold tracking-tight text-[#111827]">
          Administration
        </h1>
      </div>

      <div className="flex items-center gap-3">
        <div className="hidden h-10 items-center gap-2 rounded-xl border border-[#e3e8e5] bg-[#f8faf9] px-3 md:flex">
          <Search size={17} className="text-[#9ca3af]" />
          <input type="search" placeholder="Search operations..." className="w-52 bg-transparent text-sm outline-none placeholder:text-[#9ca3af]" />
        </div>

        <div className="hidden items-center gap-2 rounded-xl bg-[#eaf3e5] px-3 py-2 text-xs font-bold text-[#397b0a] lg:flex">
          <ShieldCheck size={16} />
          Secure session
        </div>

        <button type="button" aria-label="Notifications" className="relative rounded-xl p-2.5 text-[#6b7280] transition hover:bg-[#f5f7f6] hover:text-[#111827]">
          <Bell size={20} />
          <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-[#f5c400]" />
        </button>

        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#145323] text-sm font-extrabold text-white">
          PA
        </div>
      </div>
    </header>
  );
}