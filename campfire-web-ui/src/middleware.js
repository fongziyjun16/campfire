import {NextResponse} from "next/server";

export function middleware(request) {
    const cookies = request.cookies
    if (cookies.has("token")) {
        const requestHeader = new Headers(request.headers)
        requestHeader.set("Authorization", "Bearer " + cookies.get("token").value)
        return NextResponse.next({
            request: {
                headers: requestHeader
            }
        })
    }
    return NextResponse.next({
        request: request
    })
}

export const config = {
    matcher: ["/api/:path*"]
}