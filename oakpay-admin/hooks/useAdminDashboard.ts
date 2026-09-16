"use client";

import { useCallback, useEffect, useState } from "react";
import {
  adminApi,
  AdminDashboardStats,
} from "../lib/adminApi";

const DASHBOARD_REFRESH_MS = 15_000;

export function useAdminDashboard() {
  const [data, setData] =
    useState<AdminDashboardStats | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const result = await adminApi.dashboard();

      setData(result);
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
          : "Unable to load dashboard.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();

    const interval = window.setInterval(() => {
      if (document.visibilityState === "visible") {
        load();
      }
    }, DASHBOARD_REFRESH_MS);

    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible") {
        load();
      }
    };

    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      window.clearInterval(interval);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [load]);

  return {
    data,
    loading,
    error,
    reload: load,
  };
}
