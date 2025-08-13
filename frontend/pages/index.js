import { useState } from 'react';
import { useRouter } from 'next/router';
import api from '../lib/axios';

export default function LoginPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const router = useRouter();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        try {
            // Use a simpler email for the user to type
            const response = await api.post('/auth/login', { email: email, password: password });
            localStorage.setItem('token', response.data.accessToken);
            router.push('/dashboard');
        } catch (err) {
            setError('Failed to login. Please check your credentials.');
            console.error(err);
        }
    };

    return (
        <div className="container">
            <main className="main">
                <h1 className="title">
                    Welcome to Brand Dashboard
                </h1>

                <div style={{ marginTop: '2rem' }}>
                    <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', width: '300px' }}>
                        <h2>Login</h2>
                        <input
                            type="email"
                            placeholder="Email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            style={{ padding: '10px', marginBottom: '10px' }}
                        />
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={{ padding: '10px', marginBottom: '10px' }}
                        />
                        <button type="submit" style={{ padding: '10px', cursor: 'pointer' }}>Login</button>
                        {error && <p style={{ color: 'red', marginTop: '10px' }}>{error}</p>}
                    </form>
                </div>
            </main>
        </div>
    );
}
