"use client";

import {
  AlertTriangle,
  BarChart3,
  CheckCircle2,
  Clock3,
  Copy,
  Eye,
  RefreshCw,
  Search,
  Wallet,
  X,
} from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import AdminShell from "../../components/AdminShell";

type TradeStatus =
  | "ESCROWED"
  | "PAYMENT_PENDING"
  | "PAYMENT_MARKED"
  | "COMPLETED"
  | "CANCELLED"
  | "DISPUTED"
  | "EXPIRED"
  | string;

type AdminTransaction = {
  id: string;
  buyerId: string;
  sellerId: string;
  advertisementId: string | null;
  asset: string;
  fiatCurrency: string;
  quantity: number | string;
  unitPrice: number | string;
  fiatAmount: number | string;
  paymentMethod: string;
  status: TradeStatus;
  paymentReference: string | null;
  expiresAt: string;
  createdAt: string;
  updatedAt: string;
};

const FEED_URL = "/api/p2p-transactions?limit=200";

export default function TransactionsPage() {
  const [items, setItems] = useState<AdminTransaction[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [feedHealthy, setFeedHealthy] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [selectedTransaction, setSelectedTransaction] = useState<AdminTransaction | null>(null);
  const [copiedField, setCopiedField] = useState("");

  const load = useCallback(async (initial = false) => {
    try {
      if (initial) setLoading(true);
      else setRefreshing(true);

      const response = await fetch(FEED_URL, {
        method: "GET",
        headers: { Accept: "application/json" },
        cache: "no-store",
      });

      if (!response.ok) {
        let detail = "";
        try {
          const payload = (await response.json()) as { message?: string; detail?: string };
          detail = payload.message || payload.detail || "";
        } catch {}
        throw new Error(detail || "Unable to load live P2P transactions.");
      }

      const data = (await response.json()) as unknown;
      if (!Array.isArray(data)) throw new Error("INVALID_LIVE_FEED");

      setItems(data as AdminTransaction[]);
      setFeedHealthy(true);
      setLoadError("");
    } catch (error) {
      setFeedHealthy(false);
      // Keep the last known records visible during transient failures.
      if (initial && items.length === 0) {
        const message = error instanceof Error ? error.message : "Unable to load live P2P transactions.";
        setLoadError(message);
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    void load(true);
    const timer = window.setInterval(() => void load(false), 15000);
    return () => window.clearInterval(timer);
  }, [load]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return items;

    return items.filter((trade) =>
      [
        trade.id,
        trade.buyerId,
        trade.sellerId,
        trade.asset,
        trade.fiatCurrency,
        trade.paymentMethod,
        trade.status,
        trade.paymentReference ?? "",
      ].some((value) => String(value).toLowerCase().includes(q)),
    );
  }, [items, search]);

  const total = items.length;
  const pending = items.filter((trade) =>
    ["ESCROWED", "PAYMENT_PENDING", "PAYMENT_MARKED"].includes(trade.status),
  ).length;
  const completed = items.filter((trade) => trade.status === "COMPLETED").length;
  const disputed = items.filter((trade) => trade.status === "DISPUTED").length;

  const fmt = (value: number | string) =>
    Number(value || 0).toLocaleString(undefined, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 8,
    });

  const copyValue = async (value: string, field: string) => {
    try {
      await navigator.clipboard.writeText(value);
      setCopiedField(field);
      window.setTimeout(() => setCopiedField(""), 1500);
    } catch {
      setCopiedField("");
    }
  };

  const formatDate = (value: string) => {
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) return "—";

    return new Intl.DateTimeFormat("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(parsed);
  };

  return (
    <AdminShell>
      <div className="space-y-7">
        <header className="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#397b0a]">
              Administration
            </p>
            <h1 className="mt-1 text-4xl font-extrabold tracking-tight text-[#0b1628]">
              P2P &amp; Transactions
            </h1>
            <p className="mt-2 text-sm leading-6 text-[#667085]">
              Monitor live PayOak trades, payment progress and transaction activity.
            </p>

            <div className="mt-4 inline-flex items-center gap-2 rounded-full bg-white px-3 py-1.5 text-xs font-semibold shadow-sm ring-1 ring-[#e3e8e5]">
              <span
                className={
                  feedHealthy
                    ? "h-2 w-2 rounded-full bg-green-500"
                    : "h-2 w-2 rounded-full bg-amber-500"
                }
              />
              {feedHealthy ? "Live trade feed" : "Last known data shown"}
            </div>
          </div>

          <button
            type="button"
            onClick={() => void load(false)}
            disabled={refreshing}
            className="inline-flex w-fit items-center gap-2 rounded-xl border border-[#dce3df] bg-white px-4 py-3 text-sm font-bold text-[#082d16] shadow-sm transition hover:border-[#397b0a] disabled:cursor-not-allowed disabled:opacity-60"
          >
            <RefreshCw size={16} className={refreshing ? "animate-spin" : ""} />
            Refresh
          </button>
        </header>

{loadError ? (
          <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
            {loadError}
          </div>
        ) : null}

        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <MetricCard
            label="Transactions"
            value={loading ? "—" : total}
            helper="All P2P trades"
            icon={<BarChart3 size={21} />}
          />
          <MetricCard
            label="Pending"
            value={loading ? "—" : pending}
            helper="Awaiting payment or action"
            icon={<Clock3 size={21} />}
          />
          <MetricCard
            label="Completed"
            value={loading ? "—" : completed}
            helper="Successfully completed"
            icon={<CheckCircle2 size={21} />}
          />
          <MetricCard
            label="Disputed"
            value={loading ? "—" : disputed}
            helper="Trades needing resolution"
            icon={<AlertTriangle size={21} />}
          />
        </div>

        <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          <div className="flex flex-col gap-4 border-b border-[#e3e8e5] p-5 lg:flex-row lg:items-center lg:justify-between">
            <div className="relative max-w-2xl flex-1">
              <Search
                size={18}
                className="absolute left-4 top-1/2 -translate-y-1/2 text-[#98a2b3]"
              />
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search transaction ID, buyer, seller, asset or payment reference..."
                className="w-full rounded-xl border border-[#dce3df] bg-[#fafcfb] py-3.5 pl-11 pr-4 text-sm outline-none transition focus:border-[#397b0a] focus:ring-2 focus:ring-[#397b0a]/10"
              />
            </div>

            <div className="text-xs font-semibold text-[#667085]">
              {refreshing
                ? "Updating live data…"
                : filtered.length + " transaction" + (filtered.length === 1 ? "" : "s")}
            </div>
          </div>

          {loading ? (
            <div className="flex min-h-[360px] items-center justify-center text-sm text-[#667085]">
              Loading live transaction data…
            </div>
          ) : filtered.length === 0 ? (
            <div className="flex min-h-[360px] flex-col items-center justify-center px-6 text-center">
              <div className="rounded-2xl bg-[#edf5ee] p-4 text-[#397b0a]">
                <Wallet size={28} />
              </div>
              <h2 className="mt-5 text-lg font-extrabold text-[#111827]">
                {items.length === 0
                  ? "No P2P transactions yet"
                  : "No matching transactions"}
              </h2>
              <p className="mt-2 max-w-md text-sm leading-6 text-[#667085]">
                {items.length === 0
                  ? "New trades will appear here automatically when they are created."
                  : "Try a different transaction ID, user, asset or payment reference."}
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1180px]">
                <thead>
                  <tr className="border-b bg-[#fafcfb] text-left">
                    <Th>Transaction</Th>
                    <Th>Participants</Th>
                    <Th>Asset</Th>
                    <Th>Amount</Th>
                    <Th>Fiat total</Th>
                    <Th>Payment</Th>
                    <Th>Status</Th>
                    <Th>Created</Th>
                    <Th> </Th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((trade) => (
                    <tr
                      key={trade.id}
                      className="border-b last:border-0 hover:bg-[#fbfdfc]"
                    >
                      <td className="px-5 py-4">
                        <p className="text-sm font-bold text-[#082d16]">
                          {trade.id.slice(0, 8)}…
                        </p>
                        {trade.paymentReference ? (
                          <p className="mt-1 text-[11px] text-[#98a2b3]">
                            Ref {trade.paymentReference}
                          </p>
                        ) : null}
                      </td>

                      <td className="px-5 py-4">
                        <p className="text-xs font-semibold text-[#344054]">Buyer</p>
                        <p className="mt-0.5 text-xs text-[#667085]">
                          {trade.buyerId.slice(0, 12)}…
                        </p>
                        <p className="mt-2 text-xs font-semibold text-[#344054]">
                          Seller
                        </p>
                        <p className="mt-0.5 text-xs text-[#667085]">
                          {trade.sellerId.slice(0, 12)}…
                        </p>
                      </td>

                      <td className="px-5 py-4">
                        <p className="text-sm font-extrabold text-[#111827]">
                          {trade.asset}
                        </p>
                        <p className="mt-1 text-xs text-[#667085]">
                          {trade.unitPrice ? (
                            <>
                              {fmt(trade.unitPrice)} / {trade.asset}
                            </>
                          ) : (
                            "—"
                          )}
                        </p>
                      </td>

                      <td className="px-5 py-4 text-sm font-bold text-[#111827]">
                        {fmt(trade.quantity)} {trade.asset}
                      </td>

                      <td className="px-5 py-4 text-sm font-bold text-[#111827]">
                        {fmt(trade.fiatAmount)} {trade.fiatCurrency}
                      </td>

                      <td className="px-5 py-4 text-xs font-semibold text-[#475467]">
                        {trade.paymentMethod || "—"}
                      </td>

                      <td className="px-5 py-4">
                        <StatusBadge status={trade.status} />
                      </td>

                      <td className="px-5 py-4 text-xs text-[#667085]">
                        {formatDate(trade.createdAt)}
                      </td>

                      <td className="px-5 py-4 text-right">
                        <button
                          type="button"
                          onClick={() => setSelectedTransaction(trade)}
                          className="inline-flex items-center gap-2 rounded-lg border border-[#dce3df] bg-white px-3 py-2 text-xs font-bold text-[#082d16] transition hover:border-[#397b0a] hover:bg-[#f7fbf7]"
                        >
                          <Eye size={15} />
                          View
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>


        {selectedTransaction ? (
          <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-[#07120b]/45 p-4 backdrop-blur-[2px]"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget) setSelectedTransaction(null);
            }}
          >
            <div
              role="dialog"
              aria-modal="true"
              aria-labelledby="transaction-details-title"
              className="max-h-[90vh] w-full max-w-3xl overflow-hidden rounded-2xl border border-[#dce3df] bg-white shadow-2xl"
            >
              <div className="flex items-start justify-between border-b border-[#e3e8e5] p-6">
                <div>
                  <p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">
                    Transaction details
                  </p>
                  <h2 id="transaction-details-title" className="mt-1 text-2xl font-extrabold text-[#0b1628]">
                    {selectedTransaction.asset} P2P Trade
                  </h2>
                  <div className="mt-3 flex flex-wrap items-center gap-2">
                    <span className="rounded-full bg-[#f5f8f6] px-3 py-1 text-xs font-semibold text-[#475467]">
                      {selectedTransaction.id}
                    </span>
                    <button
                      type="button"
                      onClick={() => void copyValue(selectedTransaction.id, "transaction")}
                      className="inline-flex items-center gap-1.5 rounded-lg border border-[#dce3df] px-2.5 py-1 text-xs font-bold text-[#397b0a] hover:bg-[#f7fbf7]"
                    >
                      <Copy size={13} />
                      {copiedField === "transaction" ? "Copied" : "Copy ID"}
                    </button>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => setSelectedTransaction(null)}
                  aria-label="Close transaction details"
                  className="rounded-xl p-2 text-[#667085] hover:bg-[#f5f7f6] hover:text-[#111827]"
                >
                  <X size={22} />
                </button>
              </div>

              <div className="max-h-[calc(90vh-120px)] overflow-y-auto p-6">
                <div className="grid gap-4 sm:grid-cols-2">
                  <Detail label="Status"><StatusBadge status={selectedTransaction.status} /></Detail>
                  <Detail label="Created">{formatDate(selectedTransaction.createdAt)}</Detail>
                  <Detail label="Last updated">{formatDate(selectedTransaction.updatedAt)}</Detail>
                  <Detail label="Expires">{formatDate(selectedTransaction.expiresAt)}</Detail>
                  <Detail label="Asset">{selectedTransaction.asset}</Detail>
                  <Detail label="Fiat currency">{selectedTransaction.fiatCurrency}</Detail>
                  <Detail label="Quantity">{fmt(selectedTransaction.quantity)} {selectedTransaction.asset}</Detail>
                  <Detail label="Unit price">{fmt(selectedTransaction.unitPrice)} / {selectedTransaction.asset}</Detail>
                  <Detail label="Fiat total">{fmt(selectedTransaction.fiatAmount)} {selectedTransaction.fiatCurrency}</Detail>
                  <Detail label="Payment method">{selectedTransaction.paymentMethod || "—"}</Detail>
                  <Detail label="Payment reference">
                    {selectedTransaction.paymentReference ? (
                      <div className="flex items-center gap-2">
                        <span>{selectedTransaction.paymentReference}</span>
                        <button
                          type="button"
                          onClick={() => void copyValue(selectedTransaction.paymentReference || "", "payment")}
                          className="text-[#397b0a] hover:underline"
                        >
                          {copiedField === "payment" ? "Copied" : "Copy"}
                        </button>
                      </div>
                    ) : "—"}
                  </Detail>
                  <Detail label="Advertisement ID">
                    {selectedTransaction.advertisementId ? (
                      <button
                        type="button"
                        onClick={() => void copyValue(selectedTransaction.advertisementId || "", "ad")}
                        className="text-left text-[#397b0a] hover:underline"
                      >
                        {copiedField === "ad" ? "Copied" : selectedTransaction.advertisementId}
                      </button>
                    ) : "—"}
                  </Detail>
                </div>

                <div className="mt-6 grid gap-4 sm:grid-cols-2">
                  <IdentityCard
                    label="Buyer"
                    id={selectedTransaction.buyerId}
                    field="buyer"
                    copiedField={copiedField}
                    onCopy={copyValue}
                  />
                  <IdentityCard
                    label="Seller"
                    id={selectedTransaction.sellerId}
                    field="seller"
                    copiedField={copiedField}
                    onCopy={copyValue}
                  />
                </div>

                <div className="mt-6 rounded-xl border border-[#e3e8e5] bg-[#fafcfb] p-4">
                  <p className="text-xs font-bold uppercase tracking-[0.1em] text-[#667085]">
                    Transaction reference
                  </p>
                  <p className="mt-2 break-all font-mono text-sm text-[#111827]">
                    {selectedTransaction.id}
                  </p>
                </div>
              </div>
            </div>
          </div>
        ) : null}

        <p className="text-xs text-[#98a2b3]">
          Live data refreshes every 15 seconds. Existing records remain visible
          while an update is in progress.
        </p>
      </div>
    </AdminShell>
  );
}


function Detail({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="rounded-xl border border-[#e3e8e5] bg-[#fafcfb] p-4">
      <p className="text-[11px] font-bold uppercase tracking-[0.08em] text-[#667085]">
        {label}
      </p>
      <div className="mt-2 break-words text-sm font-semibold text-[#111827]">
        {children}
      </div>
    </div>
  );
}

function IdentityCard({
  label,
  id,
  field,
  copiedField,
  onCopy,
}: {
  label: string;
  id: string;
  field: string;
  copiedField: string;
  onCopy: (value: string, field: string) => Promise<void>;
}) {
  return (
    <div className="rounded-xl border border-[#e3e8e5] bg-white p-4">
      <p className="text-[11px] font-bold uppercase tracking-[0.08em] text-[#667085]">
        {label}
      </p>
      <p className="mt-2 break-all font-mono text-sm text-[#111827]">{id}</p>
      <button
        type="button"
        onClick={() => void onCopy(id, field)}
        className="mt-3 inline-flex items-center gap-1.5 text-xs font-bold text-[#397b0a] hover:underline"
      >
        <Copy size={13} />
        {copiedField === field ? "Copied" : "Copy ID"}
      </button>
    </div>
  );
}

function MetricCard({
  label,
  value,
  helper,
  icon,
}: {
  label: string;
  value: number | string;
  helper: string;
  icon: React.ReactNode;
}) {
  return (
    <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-[#667085]">
            {label}
          </p>
          <p className="mt-3 text-4xl font-extrabold tracking-tight text-[#0b1628]">
            {value}
          </p>
          <p className="mt-2 text-xs text-[#667085]">{helper}</p>
        </div>
        <div className="rounded-xl bg-[#edf5ee] p-3 text-[#397b0a]">{icon}</div>
      </div>
    </div>
  );
}

function Th({ children }: { children: React.ReactNode }) {
  return (
    <th className="px-5 py-4 text-[11px] font-bold uppercase tracking-[0.08em] text-[#667085]">
      {children}
    </th>
  );
}

function StatusBadge({ status }: { status: TradeStatus }) {
  const map: Record<string, string> = {
    COMPLETED: "bg-green-50 text-green-700",
    DISPUTED: "bg-red-50 text-red-700",
    CANCELLED: "bg-slate-100 text-slate-600",
    EXPIRED: "bg-slate-100 text-slate-600",
    PAYMENT_PENDING: "bg-amber-50 text-amber-700",
    PAYMENT_MARKED: "bg-blue-50 text-blue-700",
    ESCROWED: "bg-purple-50 text-purple-700",
  };

  return (
    <span
      className={"inline-flex rounded-full px-2.5 py-1 text-[11px] font-bold " +
        (map[status] || "bg-slate-100 text-slate-700")}
    >
      {status.replaceAll("_", " ")}
    </span>
  );
}
