import { NextResponse } from "next/server";

const TRADING_BASE_URL =
  process.env.OAKPAY_TRADING_URL || "http://localhost:8085";
export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const url = new URL(request.url);
    const limit = Math.min(
      Math.max(Number(url.searchParams.get("limit") || "100"), 1),
      200,
    );

    // Use the already-proven internal trading feed that Postman is able to
    // retrieve. The internal secret stays server-side in Next.js.
    const response = await fetch(
      `${TRADING_BASE_URL}/api/v1/p2p/reporting/transactions?limit=${limit}`,
      {
        method: "GET",
        headers: { Accept: "application/json" },
        cache: "no-store",
      },
    );

    const body = await response.text();

    if (!response.ok) {
      return NextResponse.json(
        {
          error: "P2P_TRANSACTION_FEED_UNAVAILABLE",
          message: `Trading service returned HTTP ${response.status} from /api/v1/internal/admin/trades.`,
          detail: body || null,
        },
        { status: response.status },
      );
    }

    let parsed: unknown;
    try {
      parsed = JSON.parse(body);
    } catch {
      return NextResponse.json(
        {
          error: "P2P_TRANSACTION_FEED_INVALID",
          message: "Trading service returned a non-JSON transaction response.",
          detail: body || null,
        },
        { status: 502 },
      );
    }

    if (!Array.isArray(parsed)) {
      return NextResponse.json(
        {
          error: "P2P_TRANSACTION_FEED_INVALID",
          message: "Trading service returned an unexpected transaction payload.",
        },
        { status: 502 },
      );
    }

    return NextResponse.json(parsed, {
      status: 200,
      headers: { "Cache-Control": "no-store" },
    });
  } catch (error) {
    return NextResponse.json(
      {
        error: "P2P_TRANSACTION_FEED_UNAVAILABLE",
        message:
          error instanceof Error
            ? error.message
            : "Unable to reach the trading service.",
      },
      { status: 502 },
    );
  }
}
