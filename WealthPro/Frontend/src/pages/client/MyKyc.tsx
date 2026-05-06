import { useState, useEffect } from 'react';
import { getKycDocs } from '@/api/clients';
import { useAuth } from '@/auth/store';

export default function MyKyc() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [docs, setDocs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (clientId) loadDocs();
  }, [clientId]);

  async function loadDocs() {
    if (!clientId) return;
    setLoading(true);
    try {
      const data = await getKycDocs(clientId);
      setDocs(data);
    } catch (e) {
      setDocs([]);
    }
    setLoading(false);
  }

  if (!clientId) {
    return <div className="p-10 text-center text-text-2">Loading account...</div>;
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-1">My KYC Documents</h1>
      <p className="text-sm text-text-2 mb-5">
        Documents collected and verified by your relationship manager.
        If you need to update any document, please contact your RM.
      </p>

      <div className="panel">
        <div className="panel-h"><h3>Your documents ({docs.length})</h3></div>

        {loading ? (
          <div className="panel-b text-center text-text-2 py-8">Loading...</div>
        ) : docs.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">📄</p>
            <p className="font-semibold">No documents on file yet</p>
            <p className="text-sm text-text-2 mt-1">
              Your relationship manager will upload your KYC documents after collecting them in person or digitally.
            </p>
          </div>
        ) : (
          <>
            {docs.some((d: any) => d.status === 'Expired') && (
              <div className="mx-5 mt-4 mb-2 p-3 rounded bg-danger/10 border border-danger/30 text-sm text-danger font-medium">
                ⚠️ One or more of your KYC documents have expired. Please contact your Relationship Manager to re-submit.
              </div>
            )}
            <table className="w-full text-sm">
            <thead className="bg-surface">
              <tr>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Type</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Reference</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Verified Date</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Expiry Date</th>
                <th className="text-left px-5 py-3 text-xs uppercase font-medium text-text-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {docs.map((d: any) => (
                <tr key={d.kycId} className={'border-t border-border-hairline' + (d.status === 'Expired' ? ' bg-danger/5' : '')}>
                  <td className="px-5 py-3 font-medium">{d.documentType}</td>
                  <td className="px-5 py-3 mono text-xs text-text-2 truncate max-w-xs">
                    {d.documentRefNumber || d.documentRef || '-'}
                  </td>
                  <td className="px-5 py-3 mono text-xs text-text-2">{d.verifiedDate || 'Pending verification'}</td>
                  <td className="px-5 py-3 mono text-xs text-text-2">{d.expiryDate || '-'}</td>
                  <td className="px-5 py-3">
                    <span className={
                      'pill ' +
                      (d.status === 'Verified' ? 'pill-success' :
                       d.status === 'Pending'  ? 'pill-warn'    :
                       d.status === 'Expired'  ? 'pill-danger'  : 'pill-danger')
                    }>{d.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
            </table>
          </>
        )}
      </div>
    </div>
  );
}
