package org.mapsforge.map.rendertheme.rule;

import java.util.List;

import org.mapsforge.core.model.Tag;

public class CB_MatchingCacheKey
{
	private final Closed closed;
	private final List<Tag> tags;
	private final byte zoomLevel;

	CB_MatchingCacheKey(List<Tag> tags, byte zoomLevel, Closed closed)
	{
		this.tags = tags;
		this.zoomLevel = zoomLevel;
		this.closed = closed;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (!(obj instanceof CB_MatchingCacheKey))
		{
			return false;
		}
		CB_MatchingCacheKey other = (CB_MatchingCacheKey) obj;
		if (this.closed != other.closed)
		{
			return false;
		}
		if (this.tags == null)
		{
			if (other.tags != null)
			{
				return false;
			}
		}
		else if (!this.tags.equals(other.tags))
		{
			return false;
		}
		if (this.zoomLevel != other.zoomLevel)
		{
			return false;
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.closed == null) ? 0 : this.closed.hashCode());
		result = prime * result + getTagListHashCode();
		result = prime * result + this.zoomLevel;
		return result;
	}

	private int getTagListHashCode()
	{
		return ((this.tags == null) ? 0 : this.tags.hashCode());
	}

}
