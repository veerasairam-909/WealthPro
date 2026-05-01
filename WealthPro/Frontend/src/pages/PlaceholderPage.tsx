interface Props {
  title: string;
  message?: string;
}

export default function PlaceholderPage({ title, message }: Props) {
  return (
    <div>
      <h1 className="text-2xl font-semibold mb-2">{title}</h1>
      {message && <p className="text-text-2 mb-6">{message}</p>}

      <div className="panel">
        <div className="panel-b text-center py-12">
          <p className="text-3xl mb-2">🚧</p>
          <p className="font-semibold">Coming soon</p>
          <p className="text-text-2 text-sm mt-1">This page will be built in a later phase.</p>
        </div>
      </div>
    </div>
  );
}
