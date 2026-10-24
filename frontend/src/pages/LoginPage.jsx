import { useEffect, useRef } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { loginWithGoogle } from '../services/authService';
import ErrorMessage from '../components/ErrorMessage';
import LoadingSpinner from '../components/LoadingSpinner';
import '../styles/login.css';

const HERO_IMAGE =
  'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1800&q=80';

const SHOWCASE = [
  {
    src: 'https://images.unsplash.com/photo-1498049794561-7780e7231661?auto=format&fit=crop&w=900&q=80',
    title: 'Laptops',
    text: 'Powerful machines for work, study and play.',
  },
  {
    src: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80',
    title: 'Audio',
    text: 'Headphones and speakers with rich, clear sound.',
  },
  {
    src: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80',
    title: 'Wearables',
    text: 'Smartwatches and trackers that keep you connected.',
  },
];

function ShieldIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M12 2 4 5v6c0 5 3.4 9.4 8 11 4.6-1.6 8-6 8-11V5l-8-3Z" />
      <path d="m9 12 2 2 4-4" />
    </svg>
  );
}

function BoltIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M13 2 4 14h7l-1 8 9-12h-7l1-8Z" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <rect x="4" y="11" width="16" height="10" rx="2" />
      <path d="M8 11V7a4 4 0 0 1 8 0v4" />
    </svg>
  );
}

function GoogleIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="google-icon">
      <path
        fill="#EA4335"
        d="M12 10.2v3.9h5.5c-.2 1.3-1.6 3.9-5.5 3.9-3.3 0-6-2.7-6-6s2.7-6 6-6c1.9 0 3.1.8 3.8 1.5l2.6-2.5C16.8 3.4 14.6 2.5 12 2.5a9.5 9.5 0 0 0 0 19c5.5 0 9.1-3.9 9.1-9.3 0-.6-.1-1.1-.2-1.6H12Z"
      />
    </svg>
  );
}

const FEATURES = [
  {
    icon: ShieldIcon,
    title: 'Secure Google sign-in',
    text: 'Sign in with the Google account you already use. No new password to remember.',
  },
  {
    icon: BoltIcon,
    title: 'Fast browsing',
    text: 'Explore the full electronics catalog with clear prices and stock status.',
  },
  {
    icon: LockIcon,
    title: 'Protected sessions',
    text: 'Your session is kept in a secure HttpOnly cookie that expires automatically.',
  },
];

export default function LoginPage() {
  const { isAuthenticated, loading } = useAuth();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const location = useLocation();
  const heroRef = useRef(null);
  const pageRef = useRef(null);
  const oauthFailed = searchParams.get('error') === 'oauth_failed';

  useEffect(() => {
    if (isAuthenticated) {
      navigate(location.state?.from || '/products', { replace: true });
    }
  }, [isAuthenticated, navigate, location.state]);

  useEffect(() => {
    if (loading) return undefined;
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    let frame = 0;

    const applyBlackHole = () => {
      frame = 0;
      const page = pageRef.current;
      if (!page) return;
      const vh = window.innerHeight;
      const vw = window.innerWidth;
      const scrollY = window.scrollY;
      page.style.setProperty('--bh', Math.min(scrollY / (vh * 0.8), 1).toFixed(3));

      page.querySelectorAll('.bh-target').forEach((el) => {
        if (el.dataset.bh === 'bg') {
          const p = Math.min(scrollY / (vh * 0.6), 1);
          el.style.filter = p > 0 ? `blur(${(p * 12).toFixed(1)}px) brightness(${(1 - p * 0.6).toFixed(2)})` : '';
          return;
        }
        const rect = el.getBoundingClientRect();
        const t =
          el.dataset.bh === 'hero'
            ? Math.min(scrollY / (vh * 0.6), 1)
            : Math.min(Math.max((vh * 0.4 - rect.top) / (vh * 0.4), 0), 1);
        if (t <= 0) {
          el.style.transform = '';
          el.style.opacity = '';
          el.style.filter = '';
          return;
        }
        const cx = rect.left + rect.width / 2;
        const cy = rect.top + rect.height / 2;
        const dx = (vw / 2 - cx) * t * 0.7;
        const dy = (vh * 0.45 - cy) * t * 0.7;
        const scale = 1 - 0.75 * t;
        const rotate = 30 * t;
        el.style.transform = `translate(${dx}px, ${dy}px) rotate(${rotate}deg) scale(${scale})`;
        el.style.opacity = (1 - Math.pow(t, 1.5)).toFixed(3);
        el.style.filter = `blur(${(t * 8).toFixed(1)}px)`;
      });
    };

    const onScroll = () => {
      if (heroRef.current) {
        heroRef.current.style.setProperty('--parallax', `${window.scrollY * 0.35}px`);
      }
      if (!reduceMotion && !frame) {
        frame = requestAnimationFrame(applyBlackHole);
      }
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => {
      window.removeEventListener('scroll', onScroll);
      cancelAnimationFrame(frame);
    };
  }, [loading]);

  useEffect(() => {
    if (loading) return undefined;
    const items = document.querySelectorAll('.reveal');
    if (!('IntersectionObserver' in window)) {
      items.forEach((el) => el.classList.add('visible'));
      return undefined;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.15 }
    );
    items.forEach((el) => observer.observe(el));
    return () => observer.disconnect();
  }, [loading]);

  if (loading) {
    return <LoadingSpinner label="Checking your session..." />;
  }

  return (
    <div className="login-page" ref={pageRef}>
      <div className="bh-core" aria-hidden="true" />
      <section className="login-hero">
        <div
          className="login-hero-bg bh-target"
          data-bh="bg"
          ref={heroRef}
          style={{ backgroundImage: `url(${HERO_IMAGE})` }}
        />
        <div className="login-hero-overlay" />
        <div className="login-hero-content glass reveal bh-target" data-bh="hero">
          <span className="login-eyebrow">ElectroMart</span>
          <h1>Welcome to ElectroMart</h1>
          <p className="login-lead">
            Discover premium electronics in one place. Sign in with Google to start browsing.
          </p>
          <ErrorMessage message={oauthFailed ? 'Google sign-in did not complete. Please try again.' : null} />
          <button type="button" className="login-cta" onClick={loginWithGoogle}>
            <GoogleIcon />
            Sign in with Google
          </button>
          <p className="login-fineprint">We only use your name, email and profile picture.</p>
        </div>
        <div className="login-scroll-hint" aria-hidden="true">Scroll to explore</div>
      </section>

      <section className="login-section">
        <h2 className="reveal bh-target">Why shop with us</h2>
        <div className="login-feature-grid">
          {FEATURES.map(({ icon: Icon, title, text }, index) => (
            <article
              key={title}
              className="glass login-feature reveal bh-target"
              style={{ transitionDelay: `${index * 120}ms` }}
            >
              <div className="login-icon">
                <Icon />
              </div>
              <h3>{title}</h3>
              <p>{text}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="login-section">
        <h2 className="reveal bh-target">Browse by category</h2>
        <div className="login-showcase">
          {SHOWCASE.map((item, index) => (
            <figure
              key={item.title}
              className="glass login-showcase-card reveal bh-target"
              style={{ transitionDelay: `${index * 120}ms` }}
            >
              <img
                src={item.src}
                alt={item.title}
                loading="lazy"
                onError={(e) => {
                  e.currentTarget.style.display = 'none';
                }}
              />
              <figcaption>
                <h3>{item.title}</h3>
                <p>{item.text}</p>
              </figcaption>
            </figure>
          ))}
        </div>
      </section>

      <section className="login-section reveal bh-target">
        <div className="glass login-final-card">
          <h2>Ready to shop?</h2>
          <p>Sign in once and your session stays active for one hour.</p>
          <button type="button" className="login-cta" onClick={loginWithGoogle}>
            <GoogleIcon />
            Sign in with Google
          </button>
        </div>
      </section>
    </div>
  );
}
