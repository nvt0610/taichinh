import type { CSSProperties } from 'react';

import { APP_NAME } from '@/config/app';

const marketRates = [
  { date: '2026-05-16', base: 'USD', quote: 'EUR', rate: 0.85827 },
  { date: '2026-05-15', base: 'USD', quote: 'VND', rate: 26251 },
  { date: '2026-05-16', base: 'USD', quote: 'JPY', rate: 145.68 },
  { date: '2026-05-16', base: 'USD', quote: 'GBP', rate: 0.73124 },
  { date: '2026-05-16', base: 'USD', quote: 'SGD', rate: 1.29641 },
  { date: '2026-05-16', base: 'USD', quote: 'AUD', rate: 1.54218 },
  { date: '2026-05-16', base: 'USD', quote: 'CAD', rate: 1.36752 },
] as const;

const goldReference = {
  brand: 'SJC 9999',
  sell: 118_500_000,
  updatedAt: '2026-05-16',
} as const;

function LoginBankCard() {
  const cardStyle = {
    '--bank-card-accent': '#d6a84f',
  } as CSSProperties;

  return (
    <article className="bank-card" style={cardStyle}>
      <div className="bank-card-top">
        <span className="bank-card-brand">{APP_NAME}</span>
        <span className="bank-card-network">VISA</span>
      </div>

      <div className="bank-card-chip" aria-hidden="true">
        <span />
        <span />
      </div>

      <div className="bank-card-number" aria-label="Số thẻ mẫu">
        4821 2048 1998 1024
      </div>

      <div className="bank-card-bottom">
        <div>
          <p className="bank-card-label">Cardholder</p>
          <strong>UWALLET USER</strong>
        </div>
        <div>
          <p className="bank-card-label">Valid thru</p>
          <strong>08/29</strong>
        </div>
      </div>
    </article>
  );
}

function LoginMarketPulse() {
  const featuredRates = marketRates.slice(0, 6);
  const latestDate = marketRates[0]?.date;

  return (
    <article className="market-pulse">
      <div className="market-pulse-head">
        <div>
          <p className="market-pulse-kicker">Thị trường</p>
          <h3>Bảng tỷ giá tham khảo</h3>
        </div>
        <span className="market-pulse-badge is-ready">Live</span>
      </div>

      <div className="market-pulse-grid">
        {featuredRates.map((item) => (
          <div className="market-pulse-metric" key={`${item.base}-${item.quote}`}>
            <span>
              {item.base} / {item.quote}
            </span>
            <strong>
              {item.quote === 'VND'
                ? new Intl.NumberFormat('vi-VN').format(item.rate)
                : item.rate.toFixed(item.rate >= 10 ? 2 : 5)}
            </strong>
          </div>
        ))}
        <p className="market-pulse-note">Cập nhật {latestDate}.</p>
      </div>
    </article>
  );
}

function HeroGoldChip() {
  return (
    <div className="hero-data-chip" aria-hidden="true">
      <span className="hero-data-chip-label">Giá vàng VND</span>
      <strong>{goldReference.brand}</strong>
      <span className="hero-data-chip-price">
        {new Intl.NumberFormat('vi-VN').format(goldReference.sell)} đ/lượng
      </span>
      <span className="hero-data-chip-meta">
        <i data-status="ready" />
        Tham chiếu {goldReference.updatedAt}
      </span>
    </div>
  );
}

export function LoginShowcase() {
  return (
    <section className="auth-showcase">
      <HeroGoldChip />

      <div className="auth-showcase-copy">
        <p className="eyebrow">{APP_NAME} Platform</p>
        <h2>Một nơi để nhìn nhanh dòng tiền, tỷ giá và các con số mình cần trước khi bắt đầu phiên làm việc.</h2>
      </div>

      <div className="auth-showcase-media">
        <LoginBankCard />
        <LoginMarketPulse />
      </div>
    </section>
  );
}
