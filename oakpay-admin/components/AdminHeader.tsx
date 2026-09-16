"use client";

import { Bell, ChevronDown, Search } from "lucide-react";

export default function AdminHeader() {
  return (
    <header className="sticky top-0 z-30 flex h-[72px] items-center justify-between border-b border-[#e7ebef] bg-white px-5 md:px-8">
      <div className="flex min-w-0 flex-1 items-center">
        <div className="flex h-10 w-full max-w-[500px] items-center gap-3 rounded-full bg-[#f1f3f7] px-4">
          <Search size={18} className="shrink-0 text-[#667085]" />
          <input
            type="search"
            aria-label="Search"
            placeholder="Search users, transactions, queries, or reference numbers..."
            className="min-w-0 flex-1 bg-transparent text-sm text-[#111827] outline-none placeholder:text-[#7a8494]"
          />
        </div>
      </div>

      <div className="ml-4 flex shrink-0 items-center gap-5">
        <button
          type="button"
          aria-label="Notifications"
          className="relative rounded-full p-2 text-[#667085] transition hover:bg-[#f5f7f6] hover:text-[#111827]"
        >
          <Bell size={21} />
          <span className="absolute right-1.5 top-1 h-3 w-3 rounded-full border-2 border-white bg-[#f5c400]" />
        </button>

        <button
          type="button"
          aria-label="Administrator profile"
          className="flex items-center gap-3 rounded-xl px-1 py-1 text-left transition hover:bg-[#f8faf9]"
        >
          <span className="flex h-11 w-11 items-center justify-center rounded-full bg-[#397b0a] text-sm font-extrabold text-white">
            RB
          </span>
          <span className="hidden leading-tight sm:block">
            <span className="block text-sm font-semibold text-[#111827]">Robson Banda</span>
            <span className="mt-1 block text-xs text-[#667085]">Administrator</span>
          </span>
          <ChevronDown size={18} className="ml-1 text-[#667085]" />
        </button>
      </div>
    </header>
  );
}
