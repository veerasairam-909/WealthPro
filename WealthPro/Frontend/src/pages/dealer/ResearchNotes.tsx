import { useState, useEffect } from 'react';
import { api } from '@/api/client';

async function getAllResearchNotes() {
  const res = await api.get('/api/research-notes');
  return res.data;
}

async function searchResearchNotes(keyword: string) {
  const res = await api.get('/api/research-notes/search', { params: { keyword } });
  return res.data;
}

export default function ResearchNotes() {
  const [notes, setNotes] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [expanded, setExpanded] = useState<number | null>(null);

  useEffect(() => {
    loadNotes();
  }, []);

  async function loadNotes() {
    setLoading(true);
    try {
      const data = await getAllResearchNotes();
      if (Array.isArray(data)) {
        setNotes(data);
      }
    } catch (e) {
    }
    setLoading(false);
  }

  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    if (!search.trim()) {
      loadNotes();
      return;
    }
    setLoading(true);
    try {
      const data = await searchResearchNotes(search.trim());
      if (Array.isArray(data)) {
        setNotes(data);
      }
    } catch (e) {
    }
    setLoading(false);
  }

  function getRatingPill(rating: string) {
    if (rating === 'BUY' || rating === 'STRONG_BUY') return 'pill-success';
    if (rating === 'SELL' || rating === 'STRONG_SELL') return 'pill-danger';
    if (rating === 'HOLD') return 'pill-warn';
    return 'pill-info';
  }

  return (
    <div>
      <div className="mb-5">
        <h1 className="text-2xl font-semibold mb-1">Research Notes</h1>
        <p className="text-sm text-text-2">
          Analyst research and security recommendations. Use for trading decisions and portfolio advisory.
        </p>
      </div>

      {/* search bar */}
      <form onSubmit={handleSearch} className="panel mb-4">
        <div className="panel-b py-3">
          <div className="flex gap-2 items-center">
            <input
              className="input flex-1 max-w-sm"
              type="text"
              placeholder="Search by keyword, symbol, analyst..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <button type="submit" className="btn btn-primary btn-sm">Search</button>
            {search && (
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => { setSearch(''); loadNotes(); }}>
                Clear
              </button>
            )}
            <span className="ml-auto text-xs text-text-2">{notes.length} notes</span>
          </div>
        </div>
      </form>

      <div className="panel">
        {loading ? (
          <div className="panel-b text-center text-text-2 py-10">Loading research notes...</div>
        ) : notes.length === 0 ? (
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">📄</p>
            <p className="font-semibold">No research notes found</p>
            <p className="text-sm text-text-2 mt-1">
              {search ? 'Try a different keyword.' : 'No research notes have been published yet.'}
            </p>
          </div>
        ) : (
          <div className="divide-y divide-border-hairline">
            {notes.map((n: any) => {
              const isExpanded = expanded === n.noteId;
              return (
                <div key={n.noteId} className="panel-b">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap mb-1">
                        <p className="font-semibold">{n.title || 'Untitled Note'}</p>
                        {n.rating && (
                          <span className={'pill ' + getRatingPill(n.rating)}>{n.rating}</span>
                        )}
                      </div>
                      <div className="flex gap-4 text-xs text-text-2 flex-wrap">
                        {n.securityId && <span>Security ID: <span className="mono font-medium">{n.securityId}</span></span>}
                        {n.analyst && <span>Analyst: <span className="font-medium">{n.analyst}</span></span>}
                        {n.publishedDate && <span>Published: {n.publishedDate}</span>}
                      </div>
                      {!isExpanded && n.content && (
                        <p className="text-sm text-text-2 mt-1 line-clamp-2">
                          {n.content.slice(0, 150)}{n.content.length > 150 ? '…' : ''}
                        </p>
                      )}
                    </div>
                    <button
                      onClick={() => setExpanded(isExpanded ? null : n.noteId)}
                      className="btn btn-ghost btn-sm shrink-0"
                    >
                      {isExpanded ? 'Collapse ▲' : 'Read ▼'}
                    </button>
                  </div>

                  {isExpanded && (
                    <div className="mt-3 bg-surface rounded-lg p-4 text-sm text-text whitespace-pre-wrap">
                      {n.content || <span className="text-text-3 italic">No content available.</span>}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
