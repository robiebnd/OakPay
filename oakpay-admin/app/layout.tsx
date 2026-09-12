import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = { title: 'OakPay Admin', description: 'OakPay operations and client management' };

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}
