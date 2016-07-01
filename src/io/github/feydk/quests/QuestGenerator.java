package io.github.feydk.quests;

public class QuestGenerator
{
	public static Quest generate(QuestType type)
	{
		Quest q = Quests.getInstance().getQuestOfType(type);
		return q.generate();
	}
}