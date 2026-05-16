import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <main className="auth-layout" aria-labelledby="not-found-title">
      <section className="auth-panel">
        <p className="eyebrow">404</p>
        <h1 id="not-found-title">Không tìm thấy trang</h1>
        <p className="lede">Đường dẫn này chưa nằm trong frontend MVP.</p>
        <Link className="primary-link-button" to="/dashboard">
          Về dashboard
        </Link>
      </section>
    </main>
  );
}
