"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { session } from "../lib/api";

export default function AdminGuard({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    const token = session.get();

    if (!token) {
      router.replace("/login");
      return;
    }

    setChecking(false);
  }, [router]);

  if (checking) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#f5f7f6]">
        <div className="text-sm font-medium text-[#6b7280]">
          Loading OakPay Admin...
        </div>
      </div>
    );
  }

  return <>{children}</>;
}