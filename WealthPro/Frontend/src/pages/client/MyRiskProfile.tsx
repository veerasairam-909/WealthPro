import { useState, useEffect } from 'react';
import { getRiskProfile } from '@/api/clients';
import { useAuth } from '@/auth/store';

const QUESTIONS = [
  { id: 'q1', text: 'What is your investment time horizon?',
    options: { A: 'Less than 1 year', B: '1-3 years', C: '3-5 years', D: 'More than 5 years' } },
  { id: 'q2', text: 'How would you react to a 20% drop in your portfolio in one month?',
    options: { A: 'Sell everything immediately', B: 'Sell some holdings', C: 'Hold and wait', D: 'Buy more at lower prices' } },
  { id: 'q3', text: 'What is your primary investment goal?',
    options: { A: 'Capital preservation', B: 'Steady income', C: 'Balanced growth', D: 'Aggressive growth' } },
  { id: 'q4', text: 'How much investment experience do you have?',
    options: { A: 'None', B: 'Basic (mutual funds)', C: 'Intermediate (stocks, ETFs)', D: 'Advanced (derivatives, structured products)' } },
  { id: 'q5', text: 'What percent of your savings are you willing to invest?',
    options: { A: 'Less than 25%', B: '25-50%', C: '50-75%', D: 'More than 75%' } },
];

export default function MyRiskProfile() {
  const user = useAuth((s) => s.user);
  const clientId = user?.clientId;

  const [risk, setRisk] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (clientId) loadRisk();
  }, [clientId]);

  async function loadRisk() {
    if (!clientId) return;
    setLoading(true);
    try {
      const data = await getRiskProfile(clientId);
      setRisk(data);
    } catch (e) {
      // 404 = not yet assessed
      setRisk(null);
    }
    setLoading(false);
  }

  if (!clientId) return <div className="p-10 text-center text-text-2">Loading account...</div>;
  if (loading)    return <div className="p-10 text-center text-text-2">Loading...</div>;

  // No risk profile yet
  if (!risk) {
    return (
      <div>
        <h1 className="text-2xl font-semibold mb-1">My Risk Profile</h1>
        <p className="text-sm text-text-2 mb-5">
          Your risk profile is determined by your relationship manager during onboarding.
        </p>
        <div className="panel">
          <div className="panel-b text-center py-10">
            <p className="text-3xl mb-2">📋</p>
            <p className="font-semibold">Risk profile not yet assessed</p>
            <p className="text-sm text-text-2 mt-1 max-w-md mx-auto">
              Your relationship manager will complete the risk assessment with you and the result will appear here.
            </p>
          </div>
        </div>
      </div>
    );
  }

  // Has a risk profile - show read-only
  let storedAnswers: any = {};
  try {
    if (typeof risk.questionnaireJSON === 'string') {
      storedAnswers = JSON.parse(risk.questionnaireJSON);
    }
  } catch (e) {
    storedAnswers = {};
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-1">My Risk Profile</h1>
      <p className="text-sm text-text-2 mb-5">
        This is the risk assessment your relationship manager has on file for you.
      </p>

      <div className="panel mb-4">
        <div className="panel-h"><h3>Summary</h3></div>
        <div className="panel-b">
          <div className="grid grid-cols-3 gap-4">
            <div>
              <p className="label">Risk class</p>
              <p className="text-2xl font-semibold mt-1">{risk.riskClass}</p>
            </div>
            <div>
              <p className="label">Risk score</p>
              <p className="text-2xl font-semibold mono mt-1">{risk.riskScore}</p>
            </div>
            <div>
              <p className="label">Assessed on</p>
              <p className="font-medium mt-1">{risk.assessedDate || '-'}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="panel">
        <div className="panel-h"><h3>Recorded answers</h3></div>
        <div className="panel-b">
          <table className="w-full text-sm">
            <tbody>
              {QUESTIONS.map((q) => (
                <tr key={q.id} className="border-b border-border-hairline">
                  <td className="py-2 pr-4 align-top w-12 mono text-text-2 text-xs">{q.id}</td>
                  <td className="py-2 pr-4 align-top text-text-2">{q.text}</td>
                  <td className="py-2 align-top font-medium">
                    {storedAnswers[q.id] ?
                      storedAnswers[q.id] + '. ' + (q.options as any)[storedAnswers[q.id]]
                      : '-'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="text-xs text-text-3 mt-4">
            If your circumstances have changed and you need a re-assessment, please contact your relationship manager.
          </p>
        </div>
      </div>
    </div>
  );
}
