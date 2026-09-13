import type { Metadata } from "next";

// @ts-expect-error - Next.js handles global CSS imports at build time.
import "./globals.css";

export const metadata: Metadata = {
  title: "OakPay Admin",
  description: "OakPay Operations Administration",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body>{children}</body>
    </html>
  );
}