"use client";

import { useEffect, useMemo, useState } from "react";
import {
  CheckCircle2,
  ChevronRight,
  Clock3,
  Search,
  Shield,
  UserCheck,
  UserX,
  Users,
  X,
} from "lucide-react";

import AdminShell from "../../components/AdminShell";
import {
  adminApi,
  AdminUser,
  AdminUserStatus,
} from "../../lib/adminApi";

type StatusFilter = "ALL" | "ACTIVE" | "INACTIVE";
type RoleFilter = "ALL" | "CLIENT" | "ADMIN";

export default function UsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] =
    useState<StatusFilter>("ALL");
  const [roleFilter, setRoleFilter] =
    useState<RoleFilter>("ALL");

  const [selectedUser, setSelectedUser] =
    useState<AdminUser | null>(null);

  const [busy, setBusy] = useState(false);

  async function loadUsers() {
    try {
      setLoading(true);
      setError("");

      const result = await adminApi.users.list(
        statusFilter === "ALL" ? undefined : statusFilter,
        roleFilter === "ALL" ? undefined : roleFilter,
      );

      setUsers(result);
    } catch (err) {
      if (
        err instanceof Error &&
        err.message === "ADMIN_AUTH_REQUIRED"
      ) {
        window.location.href = "/login";
        return;
      }

      setError(
        err instanceof Error
          ? err.message
          : "Unable to load users.",
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, [statusFilter, roleFilter]);

  const filteredUsers = useMemo(() => {
    const query = search.trim().toLowerCase();

    if (!query) {
      return users;
    }

    return users.filter((user) => {
      return (
        user.email.toLowerCase().includes(query) ||
        user.firstName.toLowerCase().includes(query) ||
        user.lastName.toLowerCase().includes(query) ||
        user.role.toLowerCase().includes(query) ||
        user.status.toLowerCase().includes(query)
      );
    });
  }, [users, search]);

  const activeCount = users.filter(
    (user) => user.status === "ACTIVE",
  ).length;

  const inactiveCount = users.filter(
    (user) => user.status === "INACTIVE",
  ).length;

  const adminCount = users.filter(
    (user) => user.role === "ADMIN",
  ).length;

  async function toggleUserStatus(user: AdminUser) {
    const nextStatus: AdminUserStatus =
      user.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";

    const confirmed = window.confirm(
      nextStatus === "INACTIVE"
        ? `Deactivate ${user.email}? They will no longer be able to authenticate.`
        : `Reactivate ${user.email}? They will be allowed to authenticate again.`,
    );

    if (!confirmed) {
      return;
    }

    try {
      setBusy(true);
      setError("");

      const updated = await adminApi.users.updateStatus(
        user.id,
        nextStatus,
      );

      setUsers((current) =>
        current.map((item) =>
          item.id === updated.id ? updated : item,
        ),
      );

      setSelectedUser(updated);
    } catch (err) {
      if (
        err instanceof Error &&
        err.message === "ADMIN_AUTH_REQUIRED"
      ) {
        window.location.href = "/login";
        return;
      }

      setError(
        err instanceof Error
          ? err.message
          : "Unable to update user status.",
      );
    } finally {
      setBusy(false);
    }
  }

  function formatDate(value: string) {
    if (!value) {
      return "—";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date);
  }

  function getInitials(user: AdminUser) {
    const first = user.firstName?.charAt(0) || "";
    const last = user.lastName?.charAt(0) || "";

    const initials = `${first}${last}`.trim();

    if (initials) {
      return initials.toUpperCase();
    }

    return user.email.charAt(0).toUpperCase();
  }

  return (
    <AdminShell>
      <div className="space-y-8">
        {/* Header */}
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <div className="mb-2 flex items-center gap-2 text-sm font-medium text-[#397b0a]">
              <Users size={16} />
              Users & Access
            </div>

            <h1 className="text-3xl font-bold tracking-tight text-[#082d16]">
              User Management
            </h1>

            <p className="mt-2 max-w-2xl text-sm leading-6 text-[#6b7280]">
              View OakPay client and administrator accounts, monitor
              account status and manage access.
            </p>
          </div>

          <button
            type="button"
            onClick={loadUsers}
            disabled={loading}
            className="inline-flex h-11 items-center justify-center rounded-xl border border-[#dfe6e1] bg-white px-4 text-sm font-semibold text-[#082d16] shadow-sm transition hover:border-[#397b0a] hover:text-[#397b0a] disabled:cursor-not-allowed disabled:opacity-60"
          >
            Refresh
          </button>
        </div>

        {/* KPI Cards */}
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <StatCard
            label="Total Users"
            value={users.length}
            icon={<Users size={20} />}
            description="Accounts currently returned"
          />

          <StatCard
            label="Active Users"
            value={activeCount}
            icon={<UserCheck size={20} />}
            description="Accounts with active access"
          />

          <StatCard
            label="Inactive Users"
            value={inactiveCount}
            icon={<UserX size={20} />}
            description="Accounts without access"
          />

          <StatCard
            label="Administrators"
            value={adminCount}
            icon={<Shield size={20} />}
            description="Accounts with admin role"
          />
        </div>

        {/* Error */}
        {error && (
          <div className="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            <UserX size={18} className="mt-0.5 shrink-0" />

            <div className="flex-1">
              <p className="font-semibold">
                Unable to complete request
              </p>

              <p className="mt-1">{error}</p>
            </div>

            <button
              type="button"
              onClick={() => setError("")}
              className="rounded-lg p-1 hover:bg-red-100"
            >
              <X size={16} />
            </button>
          </div>
        )}

        {/* Filters */}
        <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
          <div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
            {/* Search */}
            <div className="relative w-full xl:max-w-md">
              <Search
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-[#9ca3af]"
              />

              <input
                type="text"
                value={search}
                onChange={(event) =>
                  setSearch(event.target.value)
                }
                placeholder="Search name, email, role..."
                className="h-11 w-full rounded-xl border border-[#dfe6e1] bg-[#f9fbfa] pl-10 pr-4 text-sm text-[#111827] outline-none transition placeholder:text-[#9ca3af] focus:border-[#397b0a] focus:bg-white focus:ring-2 focus:ring-[#397b0a]/10"
              />
            </div>

            <div className="flex flex-wrap gap-3">
              {/* Status */}
              <div className="flex rounded-xl border border-[#dfe6e1] bg-[#f8faf9] p-1">
                {(
                  ["ALL", "ACTIVE", "INACTIVE"] as StatusFilter[]
                ).map((filter) => (
                  <button
                    key={filter}
                    type="button"
                    onClick={() =>
                      setStatusFilter(filter)
                    }
                    className={`rounded-lg px-3 py-2 text-xs font-semibold transition ${
                      statusFilter === filter
                        ? "bg-[#082d16] text-white shadow-sm"
                        : "text-[#6b7280] hover:text-[#082d16]"
                    }`}
                  >
                    {filter === "ALL"
                      ? "All"
                      : filter === "ACTIVE"
                        ? "Active"
                        : "Inactive"}
                  </button>
                ))}
              </div>

              {/* Role */}
              <select
                value={roleFilter}
                onChange={(event) =>
                  setRoleFilter(
                    event.target.value as RoleFilter,
                  )
                }
                className="h-11 rounded-xl border border-[#dfe6e1] bg-white px-4 text-sm font-medium text-[#082d16] outline-none focus:border-[#397b0a] focus:ring-2 focus:ring-[#397b0a]/10"
              >
                <option value="ALL">All Roles</option>
                <option value="CLIENT">Clients</option>
                <option value="ADMIN">Administrators</option>
              </select>
            </div>
          </div>
        </div>

        {/* Users Table */}
        <div className="overflow-hidden rounded-2xl border border-[#e3e8e5] bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-[#e9eeeb] px-6 py-5">
            <div>
              <h2 className="text-base font-bold text-[#082d16]">
                User Accounts
              </h2>

              <p className="mt-1 text-xs text-[#6b7280]">
                {filteredUsers.length} user
                {filteredUsers.length === 1 ? "" : "s"} displayed
              </p>
            </div>

            <div className="hidden items-center gap-2 text-xs font-medium text-[#6b7280] sm:flex">
              <span className="h-2 w-2 rounded-full bg-[#397b0a]" />
              Live account data
            </div>
          </div>

          {loading ? (
            <div className="flex min-h-[280px] items-center justify-center">
              <div className="flex items-center gap-3 text-sm text-[#6b7280]">
                <div className="h-5 w-5 animate-spin rounded-full border-2 border-[#dce5df] border-t-[#397b0a]" />
                Loading users...
              </div>
            </div>
          ) : filteredUsers.length === 0 ? (
            <div className="flex min-h-[280px] flex-col items-center justify-center px-6 text-center">
              <div className="mb-4 rounded-2xl bg-[#f0f5f1] p-4 text-[#397b0a]">
                <Users size={28} />
              </div>

              <h3 className="text-sm font-bold text-[#082d16]">
                No users found
              </h3>

              <p className="mt-1 max-w-sm text-xs leading-5 text-[#6b7280]">
                Try changing the status or role filter, or use a
                different search term.
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[900px]">
                <thead>
                  <tr className="border-b border-[#e9eeeb] bg-[#fafcfb] text-left">
                    <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">
                      User
                    </th>

                    <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">
                      Role
                    </th>

                    <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">
                      Status
                    </th>

                    <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">
                      Email Verification
                    </th>

                    <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">
                      Created
                    </th>

                    <th className="px-6 py-4 text-right text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">
                      Action
                    </th>
                  </tr>
                </thead>

                <tbody>
                  {filteredUsers.map((user) => (
                    <tr
                      key={user.id}
                      className="border-b border-[#eef2ef] last:border-0 hover:bg-[#fbfdfc]"
                    >
                      <td className="px-6 py-4">
                        <button
                          type="button"
                          onClick={() =>
                            setSelectedUser(user)
                          }
                          className="flex items-center gap-3 text-left"
                        >
                          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#eaf2eb] text-xs font-bold text-[#397b0a]">
                            {getInitials(user)}
                          </div>

                          <div>
                            <p className="text-sm font-semibold text-[#082d16]">
                              {user.firstName} {user.lastName}
                            </p>

                            <p className="mt-0.5 text-xs text-[#6b7280]">
                              {user.email}
                            </p>
                          </div>
                        </button>
                      </td>

                      <td className="px-6 py-4">
                        <RoleBadge role={user.role} />
                      </td>

                      <td className="px-6 py-4">
                        <StatusBadge status={user.status} />
                      </td>

                      <td className="px-6 py-4">
                        {user.emailVerified ? (
                          <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#397b0a]">
                            <CheckCircle2 size={15} />
                            Verified
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#9a6a00]">
                            <Clock3 size={15} />
                            Pending
                          </span>
                        )}
                      </td>

                      <td className="px-6 py-4 text-xs text-[#6b7280]">
                        {formatDate(user.createdAt)}
                      </td>

                      <td className="px-6 py-4">
                        <div className="flex justify-end">
                          <button
                            type="button"
                            onClick={() =>
                              setSelectedUser(user)
                            }
                            className="inline-flex items-center gap-1.5 rounded-lg px-3 py-2 text-xs font-semibold text-[#397b0a] transition hover:bg-[#edf5ee]"
                          >
                            View
                            <ChevronRight size={14} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* User Detail Panel */}
      {selectedUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#082d16]/40 p-4 backdrop-blur-sm">
          <div className="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl">
            {/* Modal header */}
            <div className="flex items-start justify-between border-b border-[#e9eeeb] px-6 py-5">
              <div className="flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[#eaf2eb] text-sm font-bold text-[#397b0a]">
                  {getInitials(selectedUser)}
                </div>

                <div>
                  <h2 className="text-lg font-bold text-[#082d16]">
                    {selectedUser.firstName}{" "}
                    {selectedUser.lastName}
                  </h2>

                  <p className="text-xs text-[#6b7280]">
                    {selectedUser.email}
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setSelectedUser(null)}
                className="rounded-lg p-2 text-[#6b7280] transition hover:bg-[#f3f6f4] hover:text-[#082d16]"
              >
                <X size={18} />
              </button>
            </div>

            {/* Details */}
            <div className="space-y-5 px-6 py-6">
              <DetailRow
                label="User ID"
                value={selectedUser.id}
              />

              <DetailRow
                label="Email"
                value={selectedUser.email}
              />

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="mb-2 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">
                    Role
                  </p>

                  <RoleBadge role={selectedUser.role} />
                </div>

                <div>
                  <p className="mb-2 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">
                    Status
                  </p>

                  <StatusBadge
                    status={selectedUser.status}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <DetailRow
                  label="Email Verified"
                  value={
                    selectedUser.emailVerified
                      ? "Yes"
                      : "No"
                  }
                />

                <DetailRow
                  label="Created"
                  value={formatDate(
                    selectedUser.createdAt,
                  )}
                />
              </div>
            </div>

            {/* Actions */}
            <div className="flex flex-col gap-3 border-t border-[#e9eeeb] bg-[#fafcfb] px-6 py-5 sm:flex-row sm:justify-end">
              <button
                type="button"
                onClick={() => setSelectedUser(null)}
                className="h-11 rounded-xl border border-[#dfe6e1] bg-white px-5 text-sm font-semibold text-[#082d16] hover:border-[#bfcac3]"
              >
                Close
              </button>

              <button
                type="button"
                disabled={busy}
                onClick={() =>
                  toggleUserStatus(selectedUser)
                }
                className={`inline-flex h-11 items-center justify-center gap-2 rounded-xl px-5 text-sm font-bold text-white transition disabled:cursor-not-allowed disabled:opacity-60 ${
                  selectedUser.status === "ACTIVE"
                    ? "bg-red-600 hover:bg-red-700"
                    : "bg-[#397b0a] hover:bg-[#2f6808]"
                }`}
              >
                {selectedUser.status === "ACTIVE" ? (
                  <>
                    <UserX size={17} />
                    {busy
                      ? "Deactivating..."
                      : "Deactivate Account"}
                  </>
                ) : (
                  <>
                    <UserCheck size={17} />
                    {busy
                      ? "Activating..."
                      : "Activate Account"}
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </AdminShell>
  );
}

/* -------------------------------------------------------------------------- */
/* Components                                                                  */
/* -------------------------------------------------------------------------- */

function StatCard({
  label,
  value,
  icon,
  description,
}: {
  label: string;
  value: number;
  icon: React.ReactNode;
  description: string;
}) {
  return (
    <div className="rounded-2xl border border-[#e3e8e5] bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-[#6b7280]">
            {label}
          </p>

          <p className="mt-3 text-3xl font-bold tracking-tight text-[#082d16]">
            {value}
          </p>
        </div>

        <div className="rounded-xl bg-[#edf5ee] p-3 text-[#397b0a]">
          {icon}
        </div>
      </div>

      <p className="mt-3 text-xs text-[#6b7280]">
        {description}
      </p>
    </div>
  );
}

function RoleBadge({ role }: { role: string }) {
  const isAdmin = role === "ADMIN";

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-1 text-[11px] font-bold ${
        isAdmin
          ? "bg-[#eef0ff] text-[#4b4f9d]"
          : "bg-[#edf5ee] text-[#397b0a]"
      }`}
    >
      {isAdmin ? "Administrator" : role}
    </span>
  );
}

function StatusBadge({
  status,
}: {
  status: AdminUserStatus;
}) {
  const active = status === "ACTIVE";

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[11px] font-bold ${
        active
          ? "bg-[#edf7ef] text-[#397b0a]"
          : "bg-[#fef2f2] text-[#b42318]"
      }`}
    >
      <span
        className={`h-1.5 w-1.5 rounded-full ${
          active ? "bg-[#397b0a]" : "bg-[#b42318]"
        }`}
      />

      {active ? "Active" : "Inactive"}
    </span>
  );
}

function DetailRow({
  label,
  value,
}: {
  label: string;
  value: string;
}) {
  return (
    <div>
      <p className="mb-1 text-[11px] font-bold uppercase tracking-wider text-[#6b7280]">
        {label}
      </p>

      <p className="break-all text-sm font-medium text-[#111827]">
        {value || "—"}
      </p>
    </div>
  );
}