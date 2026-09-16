import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "PayOak Admin",
  description: "PayOak Operations Administration",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body suppressHydrationWarning>{children}</body>
    </html>
  );
}