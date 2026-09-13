"use client";

import { FormEvent, useState } from "react";
import { Eye, EyeOff, LockKeyhole, Mail, ShieldCheck } from "lucide-react";
import { useRouter } from "next/navigation";

import { adminApi, session, TokenResponse } from "../../lib/api";

export default function Login() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const [challenge, setChallenge] = useState<string | null>(null);
  const [code, setCode] = useState("");

  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function acceptToken(token: TokenResponse) {
    session.set(token);

    /*
     * Do not use KYC summary as the authorization check.
     * The token has already been issued after authentication.
     * The dashboard itself is protected by ADMIN authorization.
     */
    router.replace("/");
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setBusy(true);

    try {
      const response = await adminApi.login(email.trim(), password);

      if (response.requiresTwoFactor) {
        setChallenge(response.challengeToken ?? null);
        return;
      }

      await acceptToken(response);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to sign in.",
      );
    } finally {
      setBusy(false);
    }
  }

  async function verifyTwoFactor(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!challenge || code.length !== 6) {
      setError("Enter the 6-digit authenticator code.");
      return;
    }

    setError("");
    setBusy(true);

    try {
      const response = await adminApi.verify2fa(
        challenge,
        code,
      );

      await acceptToken(response);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to verify the authenticator code.",
      );
    } finally {
      setBusy(false);
    }
  }

  if (challenge) {
    return (
      <main className="min-h-screen bg-[#f5f7f6] p-4 sm:p-6 lg:p-10">
        <div className="mx-auto grid min-h-[calc(100vh-3rem)] max-w-6xl overflow-hidden rounded-3xl bg-white shadow-xl lg:grid-cols-[42%_58%]">

          {/* Brand panel */}
          <section className="relative hidden overflow-hidden bg-[#082d16] p-10 text-white lg:flex lg:flex-col">
            <div className="relative z-10">
              <Logo />

              <p className="mt-20 text-xs font-bold uppercase tracking-[0.18em] text-[#f5c400]">
                Two-factor authentication
              </p>

              <h1 className="mt-5 max-w-sm text-4xl font-extrabold leading-tight">
                Verification keeps OakPay safe and secure.
              </h1>

              <p className="mt-5 max-w-sm text-sm leading-6 text-white/70">
                Protecting administrator access helps maintain
                a trusted OakPay financial ecosystem.
              </p>
            </div>

            <div className="absolute -bottom-24 -right-24 h-80 w-80 rounded-full bg-[#397b0a]/30" />
            <div className="absolute -bottom-32 -left-20 h-64 w-64 rounded-full bg-[#f5c400]/10" />

            <p className="relative z-10 mt-auto text-sm text-white/60">
              Secure operations for a brighter financial future.
            </p>
          </section>

          {/* Verification */}
          <section className="flex items-center p-7 sm:p-10 lg:p-14">
            <div className="w-full max-w-xl">
              <p className="text-xs font-bold uppercase tracking-[0.18em] text-[#397b0a]">
                Two-factor authentication
              </p>

              <h2 className="mt-3 text-3xl font-extrabold tracking-tight text-[#111827]">
                Verify administrator
              </h2>

              <p className="mt-3 text-sm text-[#6b7280]">
                Enter the 6-digit code from your authenticator app.
              </p>

              <form
                onSubmit={verifyTwoFactor}
                className="mt-9"
              >
                <label className="text-sm font-bold text-[#374151]">
                  Authenticator code
                </label>

                <input
                  value={code}
                  onChange={(e) =>
                    setCode(
                      e.target.value
                        .replace(/\D/g, "")
                        .slice(0, 6),
                    )
                  }
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  maxLength={6}
                  placeholder="000000"
                  className="mt-2 h-14 w-full rounded-xl border border-[#dce3df] bg-[#f9faf9] px-4 text-center text-2xl font-extrabold tracking-[0.55em] text-[#111827] outline-none transition focus:border-[#397b0a] focus:ring-4 focus:ring-[#397b0a]/10"
                />

                {error && (
                  <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                    {error}
                  </div>
                )}

                <button
                  type="submit"
                  disabled={busy || code.length !== 6}
                  className="mt-6 h-13 w-full rounded-xl bg-[#f59d1c] px-5 text-sm font-extrabold text-white shadow-sm transition hover:bg-[#e88e0e] disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {busy ? "Verifying..." : "Verify & continue"}
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setChallenge(null);
                    setCode("");
                    setError("");
                  }}
                  className="mt-5 w-full text-sm font-bold text-[#397b0a] hover:underline"
                >
                  Use a different account
                </button>
              </form>
            </div>
          </section>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-[#f5f7f6] p-4 sm:p-6 lg:p-10">
      <div className="mx-auto grid min-h-[calc(100vh-3rem)] max-w-6xl overflow-hidden rounded-3xl bg-white shadow-xl lg:grid-cols-[42%_58%]">

        {/* Brand panel */}
        <section className="relative hidden overflow-hidden bg-[#082d16] p-10 text-white lg:flex lg:flex-col">
          <div className="relative z-10">
            <Logo />

            <p className="mt-20 text-xs font-bold uppercase tracking-[0.18em] text-[#f5c400]">
              Admin access
            </p>

            <h1 className="mt-5 max-w-sm text-4xl font-extrabold leading-tight">
              Secure operations for a brighter financial future.
            </h1>

            <p className="mt-5 max-w-sm text-sm leading-6 text-white/70">
              Trusted. Compliant. Always on.
            </p>
          </div>

          <div className="absolute -bottom-24 -right-24 h-80 w-80 rounded-full bg-[#397b0a]/30" />
          <div className="absolute -bottom-32 -left-20 h-64 w-64 rounded-full bg-[#f5c400]/10" />

          <p className="relative z-10 mt-auto text-sm text-white/60">
            Enabling growth through trusted exchange.
          </p>
        </section>

        {/* Form */}
        <section className="flex items-center p-7 sm:p-10 lg:p-14">
          <div className="w-full max-w-xl">
            <p className="text-xs font-bold uppercase tracking-[0.18em] text-[#397b0a]">
              Admin access
            </p>

            <h2 className="mt-3 text-3xl font-extrabold tracking-tight text-[#111827]">
              Admin sign in
            </h2>

            <p className="mt-3 text-sm text-[#6b7280]">
              Secure access for authorised OakPay operations staff.
            </p>

            <form onSubmit={submit} className="mt-9 space-y-5">
              <div>
                <label className="text-sm font-bold text-[#374151]">
                  Email
                </label>

                <div className="relative mt-2">
                  <Mail className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-[#7b8790]" />

                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    autoComplete="email"
                    placeholder="admin@example.com"
                    className="h-13 w-full rounded-xl border border-[#dce3df] bg-[#f9faf9] pl-12 pr-4 text-sm text-[#111827] outline-none transition focus:border-[#397b0a] focus:ring-4 focus:ring-[#397b0a]/10"
                  />
                </div>
              </div>

              <div>
                <label className="text-sm font-bold text-[#374151]">
                  Password
                </label>

                <div className="relative mt-2">
                  <LockKeyhole className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-[#7b8790]" />

                  <input
                    type={showPassword ? "text" : "password"}
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="current-password"
                    className="h-13 w-full rounded-xl border border-[#dce3df] bg-[#f9faf9] pl-12 pr-12 text-sm text-[#111827] outline-none transition focus:border-[#397b0a] focus:ring-4 focus:ring-[#397b0a]/10"
                  />

                  <button
                    type="button"
                    onClick={() =>
                      setShowPassword((value) => !value)
                    }
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-[#7b8790] hover:text-[#397b0a]"
                    aria-label={
                      showPassword
                        ? "Hide password"
                        : "Show password"
                    }
                  >
                    {showPassword ? (
                      <EyeOff className="h-5 w-5" />
                    ) : (
                      <Eye className="h-5 w-5" />
                    )}
                  </button>
                </div>
              </div>

              {error && (
                <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={busy}
                className="h-13 w-full rounded-xl bg-[#f59d1c] px-5 text-sm font-extrabold text-white shadow-sm transition hover:bg-[#e88e0e] disabled:cursor-not-allowed disabled:opacity-50"
              >
                {busy ? "Signing in..." : "Sign in"}
              </button>

              <div className="flex items-center justify-center gap-2 pt-2 text-xs font-medium text-[#6b7280]">
                <ShieldCheck className="h-4 w-4 text-[#397b0a]" />
                Protected by multi-factor authentication
              </div>
            </form>
          </div>
        </section>
      </div>
    </main>
  );
}

function Logo() {
  return (
    <div>
      <div className="text-4xl font-extrabold tracking-tight">
        <span className="text-white">Oak</span>
        <span className="text-[#f59d1c]">Pay</span>
      </div>

      <p className="mt-1 text-sm text-white/70">
        Operations portal
      </p>
    </div>
  );
}