"use client";

import AdminGuard from "./AdminGuard";
import AdminHeader from "./AdminHeader";
import AdminSidebar from "./AdminSidebar";

export default function AdminShell({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AdminGuard>
      <div className="min-h-screen bg-[#f5f7f6]">
        <AdminSidebar />

        <div className="lg:pl-64">
          <AdminHeader />

          <main className="min-h-[calc(100vh-5rem)] p-4 sm:p-6 lg:p-8">
            <div className="mx-auto w-full max-w-[1600px]">
              {children}
            </div>
          </main>
        </div>
      </div>
    </AdminGuard>
  );
}