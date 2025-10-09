package com.a404.duckonback.service;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplateRenderer {

    private static final String LOGO = "https://duckon-bucket.s3.ap-northeast-2.amazonaws.com/image/duckon_icon.png";
    public String renderVerification(String brand, String code) {
        String html =
         """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width,initial-scale=1"/>
                  <meta http-equiv="x-ua-compatible" content="ie=edge"/>
                  <title>[DuckOn] 이메일 인증번호</title>
                  <style>
                    /* 기본 리셋 */
                    body,table,td,a{ -webkit-text-size-adjust:100%; -ms-text-size-adjust:100% }
                    table,td{ mso-table-lspace:0pt; mso-table-rspace:0pt }
                    img{ -ms-interpolation-mode:bicubic; border:0; outline:none; text-decoration:none }
                    body{ margin:0; padding:0; width:100% !important; height:100% !important }
                    /* 모바일 */
                    @media screen and (max-width:600px){
                      .container{ width:100% !important; }
                      .px{ padding-left:20px !important; padding-right:20px !important; }
                      .code{ font-size:28px !important; letter-spacing:6px !important; }
                    }
                    /* 다크모드 힌트 */
                    @media (prefers-color-scheme: dark){
                      body, .card { background:#0b0c10 !important; color:#e8eaed !important; }
                      .muted { color:#aab0b7 !important; }
                      .code-box { background:#111317 !important; border-color:#2a2f36 !important; }
                      a { color:#8ab4f8 !important; }
                    }
                    /* iOS 파란 링크 방지 */
                    a[x-apple-data-detectors]{ color:inherit !important; text-decoration:none !important }
                  </style>
                </head>
                <body style="background:#f4f6f8; font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,'Apple SD Gothic Neo','Noto Sans KR',sans-serif; color:#111827;">
                  <center style="width:100%; background:#f4f6f8;">
                    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0">
                      <tr><td style="height:24px"></td></tr>
                      <tr>
                        <td align="center">
                          <table role="presentation" class="container" width="600" cellpadding="0" cellspacing="0" border="0" style="width:600px; max-width:600px;">
                            <!-- 헤더 로고 -->
                            <!-- 헤더 로고 (3개 정렬) -->
                                   <tr>
                                     <td align="center" class="px" style="padding:16px 32px;">
                                       <table role="presentation" cellpadding="0" cellspacing="0" border="0">
                                         <tr>
                                           <td align="center" style="padding:0 8px;">
                                             <img src="https://duckon-bucket.s3.ap-northeast-2.amazonaws.com/image/duckon_icon.png"
                                                  width="56" height="56" alt="DuckOn"
                                                  style="display:block; width:56px; height:56px; border-radius:12px;"/>
                                           </td>
                                           <td align="center" style="padding:0 8px;">
                                             <img src="https://duckon-bucket.s3.ap-northeast-2.amazonaws.com/image/duckon_icon.png"
                                                  width="56" height="56" alt="DuckOn"
                                                  style="display:block; width:56px; height:56px; border-radius:12px;"/>
                                           </td>
                                           <td align="center" style="padding:0 8px;">
                                             <img src="https://duckon-bucket.s3.ap-northeast-2.amazonaws.com/image/duckon_icon.png"
                                                  width="56" height="56" alt="DuckOn"
                                                  style="display:block; width:56px; height:56px; border-radius:12px;"/>
                                           </td>
                                         </tr>
                                       </table>
                                     </td>
                                   </tr>
                
                            <!-- 카드 -->
                            <tr>
                              <td class="px" style="padding:0 32px;">
                                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0"
                                       class="card"
                                       style="background:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 2px 16px rgba(16,24,40,0.08);">
                                  <tr>
                                    <td style="padding:28px 28px 8px 28px;">
                                      <div style="font-size:12px; letter-spacing:.08em; color:#6b7280; text-transform:uppercase; font-weight:700;">
                                        DuckOn 이메일 인증
                                      </div>
                                      <h1 style="margin:6px 0 10px; font-size:22px; line-height:1.4; color:#0f172a;">
                                        요청하신 인증번호입니다
                                      </h1>
                                      <p class="muted" style="margin:0; color:#6b7280; font-size:14px; line-height:1.7;">
                                        아래 6자리 코드를 입력해 인증을 완료해주세요.
                                      </p>
                                    </td>
                                  </tr>
                
                                  <!-- 인증 코드 박스 -->
                                  <tr>
                                    <td style="padding:12px 28px 20px 28px;">
                                      <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0"
                                             class="code-box"
                                             style="background:#f8fafc; border:1px solid #e5e7eb; border-radius:12px;">
                                        <tr>
                                          <td align="center" style="padding:18px 16px;">
                                            <div class="code"
                                                 style="font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,'Liberation Mono','Courier New',monospace;
                                                        font-size:32px; letter-spacing:10px; font-weight:800; color:#111827;">
                                              {{CODE}}
                                            </div>
                                          </td>
                                        </tr>
                                      </table>
                                    </td>
                                  </tr>
                
                                  <!-- 보안 안내 -->
                                  <tr>
                                    <td style="padding:0 28px 24px 28px;">
                                      <p class="muted" style="margin:0; font-size:12px; color:#6b7280; line-height:1.7;">
                                        본 메일을 요청하지 않으셨다면, 안전을 위해 무시하셔도 됩니다.
                                      </p>
                                    </td>
                                  </tr>
                                </table>
                              </td>
                            </tr>
                
                            <!-- 푸터 -->
                            <tr>
                              <td class="px" align="center" style="padding:16px 32px 32px 32px;">
                                <p class="muted" style="margin:8px 0 0; font-size:12px; color:#9aa1a9; line-height:1.6;">
                                  © DuckOn · 이 메일은 발신 전용입니다.
                                </p>
                              </td>
                            </tr>
                
                          </table>
                        </td>
                      </tr>
                      <tr><td style="height:24px"></td></tr>
                    </table>
                  </center>
                </body>
                </html>
               """;

        return html
                .replace("{{CODE}}", code)
                .replace("DuckOn", brand) // 타이틀/본문에 브랜드명 노출
                .replace(
                        "https://duckon-bucket.s3.ap-northeast-2.amazonaws.com/image/duckon_icon.png",
                        LOGO
                );
    }
}
