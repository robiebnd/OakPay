"use client";

import { useEffect, useState } from "react";
import AdminShell from "../../components/AdminShell";
import {
  adminApi,
  KycItem,
  session,
} from "../../lib/api";

export default function Kyc() {
  const [rows, setRows] = useState<KycItem[]>([]);
  const [summary, setSummary] = useState({
    pending: 0,
    verified: 0,
    rejected: 0,
  });

  const [selected, setSelected] = useState<KycItem | null>(null);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function load() {
    const token = session.get();

    if (!token) {
      return;
    }

    try {
      setError("");

      const [summaryData, applications] = await Promise.all([
        adminApi.summary(token),
        adminApi.kyc(token),
      ]);

      setSummary(summaryData);
      setRows(applications);
    } catch (e) {
      setError(
        e instanceof Error
          ? e.message
          : "Unable to load KYC queue.",
      );
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function decide(action: "approve" | "reject") {
    if (!selected) {
      return;
    }

    const token = session.get();

    if (!token) {
      return;
    }

    if (action === "reject" && reason.trim().length < 5) {
      setError("Enter a clear rejection reason.");
      return;
    }

    setBusy(true);
    setError("");

    try {
      if (action === "approve") {
        await adminApi.approve(token, selected.kycId);
      } else {
        await adminApi.reject(
          token,
          selected.kycId,
          reason,
        );
      }

      setSelected(null);
      setReason("");

      await load();
    } catch (e) {
      setError(
        e instanceof Error
          ? e.message
          : "Review action failed.",
      );
    } finally {
      setBusy(false);
    }
  }

  return (
    <AdminShell>
      <div className="space-y-8">
        {/* Page heading */}
        <div className="flex flex-col justify-between gap-3 md:flex-row md:items-end">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.12em] text-[#397b0a]">
              Compliance
            </p>

            <h1 className="mt-1 text-2xl font-extrabold tracking-tight text-[#111827]">
              KYC Management
            </h1>

            <p className="mt-2 text-sm text-[#6b7280]">
              Review and manage client identity verification
              applications.
            </p>
          </div>

          <div className="text-sm text-[#6b7280]">
            Live verification queue
          </div>
        </div>

        {/* Error */}
        {error ? (
          <div className="rounded-xl border border-red-200 bg-red-50 px-5 py-4 text-sm font-medium text-red-700">
            {error}
          </div>
        ) : null}

        {/* KPI cards */}
        <div className="grid gap-4 md:grid-cols-3">
          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">
              Pending review
            </p>

            <p className="mt-2 text-3xl font-extrabold text-[#111827]">
              {summary.pending}
            </p>

            <p className="mt-1 text-xs text-[#6b7280]">
              Awaiting administrator review
            </p>
          </div>

          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">
              Verified
            </p>

            <p className="mt-2 text-3xl font-extrabold text-[#111827]">
              {summary.verified}
            </p>

            <p className="mt-1 text-xs text-[#6b7280]">
              Approved applications
            </p>
          </div>

          <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">
              Rejected
            </p>

            <p className="mt-2 text-3xl font-extrabold text-[#111827]">
              {summary.rejected}
            </p>

            <p className="mt-1 text-xs text-[#6b7280]">
              Returned with a reason
            </p>
          </div>
        </div>

        {/* Verification queue */}
        <section className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          <div className="flex flex-col justify-between gap-3 border-b border-[#e3e8e5] px-6 py-5 sm:flex-row sm:items-center">
            <div>
              <h2 className="text-base font-bold text-[#111827]">
                Verification queue
              </h2>

              <p className="mt-1 text-xs text-[#6b7280]">
                Client applications awaiting review.
              </p>
            </div>

            <span className="inline-flex w-fit rounded-full bg-amber-50 px-3 py-1 text-xs font-bold text-amber-700">
              {summary.pending} pending
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-left">
              <thead className="bg-[#f8faf9]">
                <tr className="border-b border-[#e3e8e5]">
                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Client
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Email
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Country
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Document
                  </th>

                  <th className="px-6 py-4 text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Submitted
                  </th>

                  <th className="px-6 py-4 text-right text-xs font-bold uppercase tracking-wide text-[#6b7280]">
                    Action
                  </th>
                </tr>
              </thead>

              <tbody>
                {rows.map((row) => (
                  <tr
                    key={row.kycId}
                    className="border-b border-[#edf1ef] last:border-b-0 hover:bg-[#fafcfb]"
                  >
                    <td className="whitespace-nowrap px-6 py-4 text-sm font-bold text-[#111827]">
                      {row.clientName}
                    </td>

                    <td className="px-6 py-4 text-sm text-[#374151]">
                      {row.email}
                    </td>

                    <td className="whitespace-nowrap px-6 py-4 text-sm text-[#6b7280]">
                      {row.country || "—"}
                    </td>

                    <td className="px-6 py-4 text-sm text-[#374151]">
                      {row.documents[0]?.documentType
                        ?.replaceAll("_", " ") || "—"}
                    </td>

                    <td className="whitespace-nowrap px-6 py-4 text-sm text-[#6b7280]">
                      {row.submittedAt
                        ? new Date(
                            row.submittedAt,
                          ).toLocaleString()
                        : "—"}
                    </td>

                    <td className="px-6 py-4 text-right">
                      <button
                        type="button"
                        onClick={() => setSelected(row)}
                        className="rounded-lg bg-[#145323] px-3 py-2 text-xs font-bold text-white transition hover:bg-[#0b3a1c]"
                      >
                        Open application
                      </button>
                    </td>
                  </tr>
                ))}

                {rows.length === 0 ? (
                  <tr>
                    <td
                      colSpan={6}
                      className="px-6 py-12 text-center text-sm text-[#6b7280]"
                    >
                      No pending KYC applications.
                    </td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      {/* KYC review modal */}
      {selected ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white shadow-2xl">
            <div className="flex items-start justify-between border-b border-[#e3e8e5] px-6 py-5">
              <div>
                <p className="text-xs font-bold uppercase tracking-[0.12em] text-[#397b0a]">
                  KYC application
                </p>

                <h2 className="mt-1 text-xl font-extrabold text-[#111827]">
                  {selected.clientName}
                </h2>

                <p className="mt-1 text-sm text-[#6b7280]">
                  {selected.email}
                  {" · "}
                  {selected.country || "Country not provided"}
                </p>
              </div>

              <button
                type="button"
                onClick={() => setSelected(null)}
                className="rounded-lg px-3 py-2 text-xl text-[#6b7280] hover:bg-[#f5f7f6] hover:text-[#111827]"
                aria-label="Close application"
              >
                ×
              </button>
            </div>

            <div className="space-y-6 px-6 py-6">
              {/* Documents */}
              <div>
                <h3 className="mb-3 text-sm font-bold text-[#111827]">
                  Identity documents
                </h3>

                <div className="grid gap-4 sm:grid-cols-2">
                  {selected.documents.map((document) => (
                    <div
                      key={document.id}
                      className="rounded-xl border border-[#e3e8e5] bg-[#f8faf9] p-4"
                    >
                      <p className="text-sm font-bold text-[#111827]">
                        {document.documentType.replaceAll(
                          "_",
                          " ",
                        )}
                      </p>

                      <p className="mt-2 text-xs text-[#6b7280]">
                        Document number
                      </p>

                      <p className="text-sm font-semibold text-[#374151]">
                        {document.documentNumber ||
                          "Number unavailable"}
                      </p>

                      <div className="mt-4 space-y-2 text-xs">
                        <div className="flex justify-between">
                          <span className="text-[#6b7280]">
                            Front
                          </span>

                          <span
                            className={
                              document.frontUploaded
                                ? "font-bold text-green-700"
                                : "font-bold text-red-600"
                            }
                          >
                            {document.frontUploaded
                              ? "Uploaded"
                              : "Missing"}
                          </span>
                        </div>

                        <div className="flex justify-between">
                          <span className="text-[#6b7280]">
                            Back
                          </span>

                          <span
                            className={
                              document.backUploaded
                                ? "font-bold text-green-700"
                                : "font-bold text-[#6b7280]"
                            }
                          >
                            {document.backUploaded
                              ? "Uploaded"
                              : "Not required / missing"}
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Rejection reason */}
              <div>
                <label
                  htmlFor="rejection-reason"
                  className="mb-2 block text-sm font-bold text-[#111827]"
                >
                  Rejection reason
                </label>

                <textarea
                  id="rejection-reason"
                  value={reason}
                  onChange={(event) =>
                    setReason(event.target.value)
                  }
                  placeholder="Required only when rejecting"
                  rows={4}
                  className="w-full rounded-xl border border-[#d9e0dc] bg-white px-4 py-3 text-sm text-[#111827] outline-none transition placeholder:text-[#9ca3af] focus:border-[#397b0a] focus:ring-2 focus:ring-[#397b0a]/10"
                />
              </div>

              {/* Actions */}
              <div className="flex flex-col-reverse gap-3 border-t border-[#e3e8e5] pt-5 sm:flex-row sm:justify-end">
                <button
                  type="button"
                  disabled={busy}
                  onClick={() => decide("reject")}
                  className="rounded-xl border border-red-200 bg-red-50 px-5 py-3 text-sm font-bold text-red-700 transition hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {busy ? "Processing..." : "Reject application"}
                </button>

                <button
                  type="button"
                  disabled={busy}
                  onClick={() => decide("approve")}
                  className="rounded-xl bg-[#397b0a] px-5 py-3 text-sm font-bold text-white transition hover:bg-[#2f6808] disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {busy ? "Processing..." : "Approve KYC"}
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </AdminShell>
  );
}