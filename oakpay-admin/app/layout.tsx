import type { Metadata } from "next";
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
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
