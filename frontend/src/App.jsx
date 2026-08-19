import React, { useState } from 'react';
import { api } from './api.js';
import { useAuthStore } from './store/authStore.js';

const toMinor = (main) => Math.round(parseFloat(main || '0') * 100);
const fromMinor = (minor) => (Number(minor) / 100).toFixed(2);

export default function App() {
  const token = useAuthStore((s) => s.token);
  const userId = useAuthStore((s) => s.userId);
  const setToken = useAuthStore((s) => s.setToken);
  const setUserId = useAuthStore((s) => s.setUserId);
  const setAuth = useAuthStore((s) => s.setAuth);
  const [log, setLog] = useState([]);

  const addLog = (entry) => setLog((l) => [{ time: new Date().toLocaleTimeString(), ...entry }, ...l]);

  const run = async (title, fn) => {
    try {
      const data = await fn();
      addLog({ ok: true, title, detail: data });
      return data;
    } catch (e) {
      addLog({ ok: false, title, detail: e.status ? `${e.status}: ${e.message}` : e.message });
      throw e;
    }
  };

  return (
    <div className="app">
      <header>
        <h1>Banking flow demo</h1>
        <p className="sub">
          Every call goes through the API gateway. {' '}
          {userId ? <span className="pill ok">signed in · {userId.slice(0, 8)}…</span>
                  : <span className="pill">not signed in</span>}
          {token ? <span className="pill ok">token set</span> : <span className="pill warn">no token</span>}
        </p>
      </header>

      <div className="layout">
        <main className="cards">
          <Register onDone={({ userId, accessToken }) => setAuth(userId, accessToken)} run={run} />
          <Login userId={userId} onUserId={setUserId} onToken={setToken} run={run} />
          <Wallets token={token} userId={userId} run={run} />
          <TopUp token={token} run={run} />
          <Transfer token={token} run={run} />
          <Ledger token={token} run={run} />
        </main>

        <aside className="logpanel">
          <div className="loghead">
            <h2>Activity</h2>
            <button className="ghost" onClick={() => setLog([])}>clear</button>
          </div>
          {log.length === 0 && <p className="muted">Actions and responses appear here.</p>}
          {log.map((e, i) => (
            <div key={i} className={`logentry ${e.ok ? 'ok' : 'err'}`}>
              <div className="logtitle">
                <span>{e.title}</span>
                <span className="logtime">{e.time}</span>
              </div>
              <pre>{typeof e.detail === 'string' ? e.detail : JSON.stringify(e.detail, null, 2)}</pre>
            </div>
          ))}
        </aside>
      </div>
    </div>
  );
}

function Card({ step, title, children, hint }) {
  return (
    <section className="card">
      <h3><span className="step">{step}</span>{title}</h3>
      {hint && <p className="hint">{hint}</p>}
      {children}
    </section>
  );
}

function Register({ onDone, run }) {
  const [f, setF] = useState({ name: 'Ada Lovelace', email: '', password: 'password123', phoneNumber: '0123456789' });
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async () => {
    const email = f.email || `user${Date.now()}@example.com`;
    const data = await run('POST /api/auth/register', () => api.register({ ...f, email }));
    if (data?.userId) onDone(data);
  };

  return (
    <Card step="1" title="Register" hint="Creates the user (via gRPC to the user service) and returns the shared userId + tokens.">
      <input placeholder="Name" value={f.name} onChange={set('name')} />
      <input placeholder="Email (blank = auto)" value={f.email} onChange={set('email')} />
      <input placeholder="Password (min 8)" value={f.password} onChange={set('password')} />
      <input placeholder="Phone (10 digits)" value={f.phoneNumber} onChange={set('phoneNumber')} />
      <button onClick={submit}>Register</button>
    </Card>
  );
}

function Login({ userId, onUserId, onToken, run }) {
  const [id, setId] = useState(userId);
  const [password, setPassword] = useState('password123');
  React.useEffect(() => setId(userId), [userId]);

  const submit = async () => {
    const data = await run('POST /api/auth/login', () => api.login({ userId: id, password }));
    if (data?.accessToken) { onToken(data.accessToken); onUserId(data.userId); }
  };

  return (
    <Card step="2" title="Login" hint="Login is by userId (the auth DB stores only userId + hashed password).">
      <input placeholder="User id" value={id} onChange={(e) => setId(e.target.value)} />
      <input placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} />
      <button onClick={submit}>Login</button>
    </Card>
  );
}

function Wallets({ token, userId, run }) {
  const [currency, setCurrency] = useState('USD');
  const [wallets, setWallets] = useState([]);

  const create = () => run('POST /api/wallets', () => api.createWallet({ userId, currency }));
  const refresh = async () => {
    const data = await run('GET /api/wallets/user/{userId}', () => api.getWallets(userId));
    setWallets(data?.wallets || []);
  };

  return (
    <Card step="3" title="Wallet" hint="Create an account to hold funds, then list this user's wallets.">
      <div className="row">
        <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
          <option>USD</option><option>EUR</option><option>GBP</option>
        </select>
        <button onClick={create} disabled={!token || !userId}>Create wallet</button>
        <button className="ghost" onClick={refresh} disabled={!token || !userId}>Refresh list</button>
      </div>
      {wallets.length > 0 && (
        <table>
          <thead><tr><th>account_no</th><th>balance</th><th>ccy</th><th>status</th></tr></thead>
          <tbody>
            {wallets.map((w) => (
              <tr key={w.walletId}>
                <td className="mono">{w.accountNo}</td>
                <td>{String(w.balance)}</td>
                <td>{w.currency}</td>
                <td>{w.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </Card>
  );
}

function TopUp({ token, run }) {
  const [accountNo, setAccountNo] = useState('');
  const [amount, setAmount] = useState('50.00');
  const [currency, setCurrency] = useState('USD');

  const submit = () => run('POST /api/payment/topup', () =>
    api.topUp({ accountNo, amount: toMinor(amount), currency }));

  return (
    <Card step="4" title="Top up" hint="Credits an account. Payment publishes a Kafka event that the ledger records.">
      <input placeholder="account_no" value={accountNo} onChange={(e) => setAccountNo(e.target.value)} />
      <input placeholder="Amount (e.g. 50.00)" value={amount} onChange={(e) => setAmount(e.target.value)} />
      <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
        <option>USD</option><option>EUR</option><option>GBP</option>
      </select>
      <button onClick={submit} disabled={!token}>Top up</button>
    </Card>
  );
}

function Transfer({ token, run }) {
  const [fromAccount, setFrom] = useState('');
  const [toAccount, setTo] = useState('');
  const [amount, setAmount] = useState('25.00');
  const [currency, setCurrency] = useState('USD');

  const submit = () => run('POST /api/payment/transfer', () =>
    api.transfer({ fromAccount, toAccount, amount: toMinor(amount), currency }));

  return (
    <Card step="5" title="Transfer" hint="Fraud-checked before it runs. Try > 10,000.00 to see the fraud service deny it (403).">
      <input placeholder="from account_no" value={fromAccount} onChange={(e) => setFrom(e.target.value)} />
      <input placeholder="to account_no" value={toAccount} onChange={(e) => setTo(e.target.value)} />
      <input placeholder="Amount (e.g. 25.00)" value={amount} onChange={(e) => setAmount(e.target.value)} />
      <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
        <option>USD</option><option>EUR</option><option>GBP</option>
      </select>
      <button onClick={submit} disabled={!token}>Transfer</button>
    </Card>
  );
}

function Ledger({ token, run }) {
  const [accountNo, setAccountNo] = useState('');
  const [rows, setRows] = useState([]);

  const load = async () => {
    const data = await run('GET /api/ledger/accounts/{acct}/transactions', () =>
      api.ledgerByAccount(accountNo));
    setRows(Array.isArray(data) ? data : []);
  };

  return (
    <Card step="6" title="Ledger" hint="Everything the ledger service recorded from Kafka for an account.">
      <div className="row">
        <input placeholder="account_no" value={accountNo} onChange={(e) => setAccountNo(e.target.value)} />
        <button onClick={load} disabled={!token}>Load transactions</button>
      </div>
      {rows.length > 0 && (
        <table>
          <thead><tr><th>type</th><th>from</th><th>to</th><th>amount</th><th>ccy</th><th>when</th></tr></thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id || r.transactionId}>
                <td>{r.type}</td>
                <td className="mono">{r.fromAccount || '—'}</td>
                <td className="mono">{r.toAccount}</td>
                <td>{fromMinor(r.amount)}</td>
                <td>{r.currency}</td>
                <td>{String(r.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </Card>
  );
}
