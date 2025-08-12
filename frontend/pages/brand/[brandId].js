import { useRouter } from 'next/router';
import { useEffect, useState, createRef } from 'react';
import api from '../../lib/axios';
import { embedDashboard } from "@superset-ui/embedded-sdk";

export default function BrandPage() {
    const router = useRouter();
    const { brandId } = router.query;
    const [brand, setBrand] = useState(null);
    const [metrics, setMetrics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const dashboardContainer = createRef();

    useEffect(() => {
        if (!brandId) return;

        const fetchData = async () => {
            setLoading(true);
            setError('');
            try {
                const brandRes = await api.get(`/brands/${brandId}`);
                setBrand(brandRes.data);

                const metricsRes = await api.get(`/brands/${brandId}/metrics/summary`);
                setMetrics(metricsRes.data);

                // Fetch the guest token from the backend
                const tokenRes = await api.get(`/brands/${brandId}/reports/iframe`);
                const embedToken = tokenRes.data.token;

                // Embed the dashboard using the SDK
                if (dashboardContainer.current) {
                    embedDashboard({
                        id: "1", // The same UUID used in the backend
                        supersetDomain: "http://localhost:8088",
                        mountPoint: dashboardContainer.current,
                        fetchGuestToken: () => Promise.resolve(embedToken),
                        dashboardUiConfig: {
                            hideTitle: true,
                            hideChartControls: true,
                            hideTab: true,
                        },
                    });
                }

            } catch (err) {
                setError('Failed to fetch or embed brand data.');
                console.error(err);
                if (err.response && err.response.status === 403) {
                    router.push('/');
                }
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [brandId, router, dashboardContainer]);

    if (error) return <p style={{ color: 'red' }}>{error}</p>;
    if (!brand && loading) return <p>Loading...</p>;
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

                <div style={{ marginTop: '2rem', width: '100%', height: '600px', border: '1px solid #ddd' }}>
                    <h2>Superset Dashboard</h2>
                    <div ref={dashboardContainer} style={{ width: '100%', height: '100%' }}>
                        {loading && <p>Loading dashboard...</p>}
                    </div>
                </div>
            </main>
        </div>
    );
}
