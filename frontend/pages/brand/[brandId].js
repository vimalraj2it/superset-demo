import { useRouter } from 'next/router';
import { useEffect, useState } from 'react';
import api from '../../lib/axios';

export default function BrandPage() {
    const router = useRouter();
    const { brandId } = router.query;
    const [brand, setBrand] = useState(null);
    const [metrics, setMetrics] = useState(null);
    const [redashUrl, setRedashUrl] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!brandId) return;

        const fetchData = async () => {
            setLoading(true);
            setError('');
            try {
                // Fetch brand and metrics data
                const brandRes = await api.get(`/brands/${brandId}`);
                setBrand(brandRes.data);

                const metricsRes = await api.get(`/brands/${brandId}/metrics/summary`);
                setMetrics(metricsRes.data);

                // Fetch the signed URL for the Redash dashboard
                const redashRes = await api.get(`/brands/${brandId}/reports/redash-iframe`);
                setRedashUrl(redashRes.data.url);

            } catch (err) {
                setError('Failed to fetch brand data or dashboard URL.');
                console.error(err);
                if (err.response && err.response.status === 403) {
                    router.push('/'); // Redirect if not authorized
                }
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [brandId, router]);

    if (error) return <p style={{ color: 'red' }}>{error}</p>;
    if (loading) return <p>Loading...</p>;
    if (!brand) return <p>No brand data found.</p>;

    return (
        <div className="container">
            <main className="main">
                <h1 className="title">{brand.name}</h1>

                <div style={{ marginTop: '2rem', width: '100%', maxWidth: '800px' }}>
                    <h2>Key Metrics</h2>
                    {metrics ? (
                        <div style={{ display: 'flex', justifyContent: 'space-around', textAlign: 'center' }}>
                            <div>
                                <h3>Total Orders</h3>
                                <p>{metrics.totalOrders}</p>
                            </div>
                            <div>
                                <h3>Total Revenue</h3>
                                <p>${metrics.totalRevenue.toFixed(2)}</p>
                            </div>
                            <div>
                                <h3>Avg. Order Value</h3>
                                <p>${metrics.avgOrderValue.toFixed(2)}</p>
                            </div>
                            <div>
                                <h3>Active Products</h3>
                                <p>{metrics.activeProducts}</p>
                            </div>
                        </div>
                    ) : (
                        <p>Loading metrics...</p>
                    )}
                </div>

                <div style={{ marginTop: '2rem', width: '100%', height: '800px', border: '1px solid #ddd' }}>
                    <h2>Redash Dashboard</h2>
                    {redashUrl ? (
                        <iframe
                            src={redashUrl}
                            title="Redash Dashboard"
                            style={{ width: '100%', height: '100%', border: 'none' }}
                        />
                    ) : (
                        <p>Loading dashboard...</p>
                    )}
                </div>
            </main>
        </div>
    );
}
