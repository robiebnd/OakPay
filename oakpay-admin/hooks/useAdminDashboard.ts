"use client";

import { useCallback, useEffect, useState } from "react";
import {
  adminApi,
  AdminDashboardStats,
//} from "@/lib/adminApi";
} from "../lib/adminApi";

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
  }, [load]);

  return {
    data,
    loading,
    error,
    reload: load,
  };
}