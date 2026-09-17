import { NextResponse } from "next/server";

const TRADING_BASE_URL = process.env.OAKPAY_TRADING_URL || "http://localhost:8085";
const INTERNAL_SECRET = process.env.OAKPAY_INTERNAL_SECRET || "oakpay-internal-development-secret-change-before-production";

export async function GET(request: Request) {
  try {
    const url = new URL(request.url);
    const limit = Math.min(Math.max(Number(url.searchParams.get("limit") || "100"), 1), 200);

    const response = await fetch(
      `${TRADING_BASE_URL}/api/v1/internal/admin/trades?limit=${limit}`,
      {
        method: "GET",
        headers: {
          Accept: "application/json",
          "X-OakPay-Internal-Secret": INTERNAL_SECRET,
        },
        cache: "no-store",
      },
    );

    const body = await response.text();

    if (!response.ok) {
      return NextResponse.json(
        {
          error: "P2P_TRANSACTION_FEED_UNAVAILABLE",
          message: `Trading service returned HTTP ${response.status}.`,
          detail: body || null,
        },
        { status: response.status },
      );
    }

    return new NextResponse(body, {
      status: 200,
      headers: { "Content-Type": "application/json", "Cache-Control": "no-store" },
    });
  } catch (error) {
    return NextResponse.json(
      {
        error: "P2P_TRANSACTION_FEED_UNAVAILABLE",
        message: error instanceof Error ? error.message : "Unable to reach the trading service.",
      },
      { status: 502 },
    );
  }
}
